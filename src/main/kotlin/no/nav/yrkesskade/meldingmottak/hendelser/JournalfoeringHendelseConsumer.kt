package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer {

    @KafkaListener(id = "yrkesskade-melding-mottak",
                   topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
                   containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
                   idIsGroup = false)
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        println(record)
    }
}