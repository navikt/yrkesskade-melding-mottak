package no.nav.yrkesskade.meldingmottak.config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@TestConfiguration
class KafkaConfig {
    @Bean
    fun schemaRegistryClient(): MockSchemaRegistryClient {
        return MockSchemaRegistryClient()
    }

    @Bean
    fun kafkaConsumerFactory(properties: KafkaProperties,
                             schemaRegistryClient: MockSchemaRegistryClient
    ): ConsumerFactory<String?, Any?>? {
        val consumerProperties = properties.buildConsumerProperties()
        consumerProperties["specific.avro.reader"] = "true"
        return DefaultKafkaConsumerFactory(
            consumerProperties,
            StringDeserializer(),
            KafkaAvroDeserializer(schemaRegistryClient, consumerProperties)
        )
    }

    @Bean
    fun kafkaJournalfoeringHendelseListenerContainerFactory(kafkaConsumerFactory: ConsumerFactory<String?, Any?>):
            ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {
        return ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            this.setConsumerFactory(kafkaConsumerFactory)
        }
    }

    @Bean
    fun kafkaProducerFactory(properties: KafkaProperties,
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
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }
}