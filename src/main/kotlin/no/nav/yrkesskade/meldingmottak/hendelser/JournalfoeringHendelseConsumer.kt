package no.nav.yrkesskade.meldingmottak.hendelser

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import kotlinx.coroutines.runBlocking
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional
import javax.ws.rs.core.HttpHeaders


@Service
class JournalfoeringHendelseConsumer(
    @Value("\${saf.graphql.url}") private val safGraphqlUrl: String,
    private val tokenUtil: TokenUtil
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    val safClient = GraphQLWebClient(url = safGraphqlUrl)

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
        val journalpostQuery = Journalpost(Journalpost.Variables(journalpostId))

        val oppdatertJournalpost: Journalpost.Result?
        runBlocking {
            val response: GraphQLClientResponse<Journalpost.Result> = safClient.execute(journalpostQuery) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }
            oppdatertJournalpost = response.data
        }
        return oppdatertJournalpost
    }
}