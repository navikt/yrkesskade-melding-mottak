package no.nav.yrkesskade.meldingmottak.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate


private const val ANTALL_RETRIES = 10

private const val ETT_SEKUND = 1000L

private const val NO_NAV_YRKESSKADE_MODEL = "no.nav.yrkesskade.model"

@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun kafkaJournalfoeringHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {

        val consumerProperties = kafkaProperties.buildConsumerProperties().apply {
            this[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class
        }
        val consumerFactory = DefaultKafkaConsumerFactory<String, JournalfoeringHendelseRecord>(consumerProperties)

        return ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            this.setConsumerFactory(consumerFactory)
            this.setErrorHandler(ContainerStoppingErrorHandler())
            this.setRetryTemplate(retryTemplate())
        }
    }

    @Bean
    fun skademeldingInnsendtHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, SkademeldingInnsendtHendelse> {
        val consumerFactory = DefaultKafkaConsumerFactory<String, SkademeldingInnsendtHendelse>(
            kafkaProperties.buildConsumerProperties().apply {
                this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
                this[JsonDeserializer.TRUSTED_PACKAGES] = NO_NAV_YRKESSKADE_MODEL
            }
        )

        return ConcurrentKafkaListenerContainerFactory<String, SkademeldingInnsendtHendelse>().apply {
            this.setConsumerFactory(consumerFactory)
            this.setErrorHandler(ContainerStoppingErrorHandler())
            this.setRetryTemplate(retryTemplate())
        }
    }

    fun retryTemplate() = RetryTemplate().apply {
        this.setBackOffPolicy(ExponentialBackOffPolicy().apply {
            this.initialInterval = ETT_SEKUND
        })
        this.setRetryPolicy(SimpleRetryPolicy(ANTALL_RETRIES))
    }
}
