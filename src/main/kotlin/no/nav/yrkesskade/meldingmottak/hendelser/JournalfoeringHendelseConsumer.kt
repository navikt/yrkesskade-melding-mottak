package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCorrelation
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer(
    private val journalfoeringHendelseService: JournalfoeringHendelseService
) {

    @KafkaListener(
        id = "yrkesskade-melding-mottak",
        topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
        containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        kallMetodeMedCorrelation { journalfoeringHendelseService.prosesserJournalfoeringHendelse(record) }
    }
}