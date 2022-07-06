package no.nav.yrkesskade.meldingmottak.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandling
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate


private const val ANTALL_RETRIES = 10

private const val ETT_SEKUND = 1000L

@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun producerProperties(kafkaProperties: KafkaProperties): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap(kafkaProperties.buildProducerProperties())
        return props
    }

    @Bean
    fun dokumentTilSaksbehandlingProducerFactory(
        producerProperties: Map<String, Any>
    ): ProducerFactory<String, DokumentTilSaksbehandling> {
        return DefaultKafkaProducerFactory(producerProperties)
    }

    @Bean
    fun dokumentTilSaksbehandlingKafkaTemplate(
        dokumentTilSaksbehandlingProducerFactory: ProducerFactory<String, DokumentTilSaksbehandling>
    ): KafkaTemplate<String, DokumentTilSaksbehandling> {
        return KafkaTemplate(dokumentTilSaksbehandlingProducerFactory)
    }

    @Bean
    fun kafkaJournalfoeringHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {

        val consumerProperties = kafkaProperties.buildConsumerProperties().apply {
            this[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
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

        val consumerProperties = kafkaProperties.buildConsumerProperties().apply {
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        }
        val consumerFactory = DefaultKafkaConsumerFactory<String, SkademeldingInnsendtHendelse>(consumerProperties)

        return ConcurrentKafkaListenerContainerFactory<String, SkademeldingInnsendtHendelse>().apply {
            this.setConsumerFactory(consumerFactory)
            this.setErrorHandler(ContainerStoppingErrorHandler())
            this.setRetryTemplate(retryTemplate())
        }
    }

    @Bean
    fun skadeforklaringInnsendingHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, SkadeforklaringInnsendingHendelse> {

        val consumerProperties = kafkaProperties.buildConsumerProperties().apply {
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        }
        val consumerFactory = DefaultKafkaConsumerFactory<String, SkadeforklaringInnsendingHendelse>(consumerProperties)

        return ConcurrentKafkaListenerContainerFactory<String, SkadeforklaringInnsendingHendelse>().apply {
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
