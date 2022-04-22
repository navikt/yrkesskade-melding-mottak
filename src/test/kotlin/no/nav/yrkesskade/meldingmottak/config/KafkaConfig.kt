package no.nav.yrkesskade.meldingmottak.config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

private const val ANTALL_RETRIES = 10

private const val ETT_SEKUND = 1000L

@TestConfiguration
class KafkaConfig {

    @Bean
    fun schemaRegistryClient(): MockSchemaRegistryClient {
        return MockSchemaRegistryClient()
    }

    @Bean
    fun kafkaAvroConsumerFactory(
        properties: KafkaProperties,
        schemaRegistryClient: MockSchemaRegistryClient
    ): ConsumerFactory<String?, Any?>? {
        val consumerProperties = properties.buildConsumerProperties().apply {
            this[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class
        }

        return DefaultKafkaConsumerFactory(
            consumerProperties,
            StringDeserializer(),
            KafkaAvroDeserializer(schemaRegistryClient, consumerProperties)
        )
    }

    @Bean
    fun kafkaJournalfoeringHendelseListenerContainerFactory(kafkaAvroConsumerFactory: ConsumerFactory<String?, Any?>):
            ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {
        return ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            this.setConsumerFactory(kafkaAvroConsumerFactory)
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
            }
        )

        return ConcurrentKafkaListenerContainerFactory<String, SkademeldingInnsendtHendelse>().apply {
            this.setConsumerFactory(consumerFactory)
            this.setErrorHandler(ContainerStoppingErrorHandler())
            this.setRetryTemplate(retryTemplate())
        }
    }

    @Bean
    fun skadeforklaringInnsendingHendelseListenerContainerFactory(kafkaProperties: KafkaProperties):
            ConcurrentKafkaListenerContainerFactory<String, SkadeforklaringInnsendingHendelse> {
        val consumerFactory = DefaultKafkaConsumerFactory<String, SkadeforklaringInnsendingHendelse>(
            kafkaProperties.buildConsumerProperties().apply {
                this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
            }
        )

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

    @Bean
    fun journalfoeringHendelseProducerFactory(
        properties: KafkaProperties,
        schemaRegistryClient: MockSchemaRegistryClient
    ): ProducerFactory<String, Any> {
        val producerProperties = properties.buildProducerProperties()
        return DefaultKafkaProducerFactory(
            producerProperties,
            StringSerializer(),
            KafkaAvroSerializer(schemaRegistryClient, producerProperties)
        )
    }

    @Bean
    fun skademeldingProducerFactory(properties: KafkaProperties): DefaultKafkaProducerFactory<String, SkademeldingInnsendtHendelse> =
        DefaultKafkaProducerFactory(
            properties.buildProducerProperties().apply {
                this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
            }
        )

    @Bean
    fun skadeforklaringProducerFactory(properties: KafkaProperties): DefaultKafkaProducerFactory<String, SkadeforklaringInnsendingHendelse> =
        DefaultKafkaProducerFactory(
            properties.buildProducerProperties().apply {
                this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
            }
        )

    @Bean
    fun journalfoeringHendelseKafkaTemplate(
        journalfoeringHendelseProducerFactory: ProducerFactory<String, Any>
    ): KafkaTemplate<String, Any> = KafkaTemplate(journalfoeringHendelseProducerFactory)

    @Bean
    fun skademeldingKafkaTemplate(
        skademeldingProducerFactory: ProducerFactory<String, SkademeldingInnsendtHendelse>
    ): KafkaTemplate<String, SkademeldingInnsendtHendelse> = KafkaTemplate(skademeldingProducerFactory)

    @Bean
    fun skadeforklaringKafkaTemplate(
        skadeforklaringProducerFactory: ProducerFactory<String, SkadeforklaringInnsendingHendelse>
    ): KafkaTemplate<String, SkadeforklaringInnsendingHendelse> = KafkaTemplate(skadeforklaringProducerFactory)

}