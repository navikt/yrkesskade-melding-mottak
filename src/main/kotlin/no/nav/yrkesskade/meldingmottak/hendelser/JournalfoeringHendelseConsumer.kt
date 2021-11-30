package no.nav.yrkesskade.meldingmottak.hendelser

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.generated.Journalpost
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import okhttp3.MediaType.Companion.toMediaType
import org.springframework.http.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer(
    @Value("\${saf.graphql.url}") private val safGraphqlUrl: String,
    private val tokenUtil: TokenUtil
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    val safClient = GraphQLWebClient(url = safGraphqlUrl)
    private val httpClient = OkHttpClient()

    @KafkaListener(
        id = "yrkesskade-melding-mottak",
        topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
        containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        if (record.temaNytt.equals("YRK")) {
            log.info(record.toString())

            val oppdatertJournalpost = hentOppdatertJournalpost(record.journalpostId.toString())
            if (oppdatertJournalpost == null) {
                log.warn("Fant ikke journalpost i SAF for journalpostId ${record.journalpostId}")
            } else {
                log.info("Oppdatert journalpost for journalpostId ${record.journalpostId}: $oppdatertJournalpost")
            }
        }
    }

    fun hentOppdatertJournalpost(journalpostId: String): Journalpost.Result? {
        val token = tokenUtil.getAppAccessTokenWithSafScope()
        log.info("Hentet token")

        log.info("Henter oppdatert journalpost for id $journalpostId på url $safGraphqlUrl")
        val graphQLQuery =
            this::class.java.getResource("/graphql/journalpost.graphql")
                .readText().replace("[\n\r]", "")

        val query = JournalpostQuery(query = graphQLQuery, variables = """{journalpostId: $journalpostId}""")
        val postBody = jacksonObjectMapper().writeValueAsString(query)
        val request = Request.Builder()
            .url(safGraphqlUrl)
            .post(postBody.toRequestBody(MediaType.APPLICATION_JSON_VALUE.toMediaType()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Unexpected code $response")
            return jacksonObjectMapper().readValue(response.body!!.string())
        }
    }
}

data class JournalpostQuery(
    val query: String,
    val variables: String
)
