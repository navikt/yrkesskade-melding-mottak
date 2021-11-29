package no.nav.yrkesskade.meldingmottak.hendelser

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import kotlinx.coroutines.runBlocking
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer(@Value("\${saf.graphql.url}") private val safGraphqlUrl: String) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    val safClient = GraphQLWebClient(url = safGraphqlUrl)

    @KafkaListener(id = "yrkesskade-melding-mottak",
            topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
            containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
            idIsGroup = false)
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        log.info(record.toString())

        runBlocking {
            val response: GraphQLClientResponse<Journalpost.Result> = safClient.execute(
                    Journalpost(variables = Journalpost.Variables(journalpostId = record.journalpostId.toString()))
            )
            log.info(response.data.toString())
        }
    }
}