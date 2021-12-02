package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer(private val safClient: SafClient) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

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

            val oppdatertJournalpost = safClient.hentOppdatertJournalpost(record.journalpostId.toString())
            if (oppdatertJournalpost == null) {
                log.warn("Fant ikke journalpost i SAF for journalpostId ${record.journalpostId}")
            } else {
                log.info("Oppdatert journalpost for journalpostId ${record.journalpostId}: $oppdatertJournalpost")
            }
        }
    }


}