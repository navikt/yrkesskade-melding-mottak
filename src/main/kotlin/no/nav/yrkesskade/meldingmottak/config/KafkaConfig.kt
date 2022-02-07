package no.nav.yrkesskade.meldingmottak.config

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate


private const val ANTALL_RETRIES = 10

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
            this.setErrorHandler(ContainerStoppingErrorHandler())
            this.setRetryTemplate(
                RetryTemplate().apply {
                    this.setBackOffPolicy(ExponentialBackOffPolicy())
                    this.setRetryPolicy(SimpleRetryPolicy(ANTALL_RETRIES))
                }
            )
        }
    }
}
