package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer {

    @KafkaListener(id = "yrkesskade-melding-mottak",
                   topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
                   containerFactory = "kafkaListenerContainerFactory",
                   idIsGroup = false)
    @Transactional
    fun listen(consumerRecord: ConsumerRecord<Long, JournalfoeringHendelseRecord>, ack: Acknowledgment) {
        println("${consumerRecord.value()}")
    }
}