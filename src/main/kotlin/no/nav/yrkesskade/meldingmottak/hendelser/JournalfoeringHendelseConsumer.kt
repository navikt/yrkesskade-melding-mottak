package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional


private const val TEMA_YRKESSKADE = "YRK"

@Service
class JournalfoeringHendelseConsumer(
    private val journalfoeringHendelseService: JournalfoeringHendelseService
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    @KafkaListener(
        id = "yrkesskade-melding-mottak",
        topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
        containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        if (record.temaNytt.equals(TEMA_YRKESSKADE)) {
            log.info("Mottatt journalføringhendelse: $record")
            journalfoeringHendelseService.prosesserJournalfoeringHendelse(record)
        }
    }
}