package no.nav.yrkesskade.meldingmottak.config

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory


@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun kafkaJournalfoeringHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {
        val consumerProperties = kafkaProperties.buildConsumerProperties()
        consumerProperties["specific.avro.reader"] = "true"
        val consumerFactory = DefaultKafkaConsumerFactory<String, JournalfoeringHendelseRecord>(consumerProperties)
        return ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            this.setConsumerFactory(consumerFactory)
        }
    }

//    @Bean
//    fun kafkaJournalføringHendelseListenerContainerFactory(properties: KafkaProperties, kafkaErrorHandler: KafkaErrorHandler)
//            : ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord> {
//        properties.properties.put("specific.avro.reader", "true")
//        val factory = ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord>()
//        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
//        factory.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(2)
//        factory.consumerFactory = DefaultKafkaConsumerFactory(properties.buildConsumerProperties())
//        factory.setErrorHandler(kafkaErrorHandler)
//        return factory
//    }
}
