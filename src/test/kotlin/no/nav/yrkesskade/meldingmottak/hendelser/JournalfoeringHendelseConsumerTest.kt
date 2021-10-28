package no.nav.yrkesskade.meldingmottak.hendelser

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka(partitions = 1, ports = [9092])
internal class JournalfoeringHendelseConsumerTest {

    @Autowired
    lateinit var journalfoeringHendelseConsumer: JournalfoeringHendelseConsumer

    @Autowired
    lateinit var kafkaProperties: KafkaProperties

    lateinit var kafkaProducer: KafkaProducer<String, JournalfoeringHendelseRecord>

    init {
        PostgresDockerContainer.container
    }

    @Test
    fun listen() {
        kafkaProducer = KafkaProducer<String, JournalfoeringHendelseRecord>(
                kafkaProperties.buildProducerProperties(),
                StringSerializer(),
                KafkaAvroSerializer()
        )
        kafkaProducer.send(ProducerRecord("test", JournalfoeringHendelseRecord()))
    }

    @TestConfiguration
    class Config {
        @Bean
        fun schemaRegistryClient(): MockSchemaRegistryClient {
            return MockSchemaRegistryClient()
        }

        @Bean
        fun kafkaConsumerFactory(properties: KafkaProperties,
                                 schemaRegistryClient: MockSchemaRegistryClient): ConsumerFactory<String?, Any?>? {
            val consumerProperties = properties.buildConsumerProperties()
            consumerProperties["specific.avro.reader"] = "true"
            return DefaultKafkaConsumerFactory(
                    consumerProperties,
                    StringDeserializer(),
                    KafkaAvroDeserializer(schemaRegistryClient, consumerProperties)
            )
        }

        @Bean
        fun kafkaListenerContainerFactory(kafkaConsumerFactory: ConsumerFactory<String?, Any?>): ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord> {
            return ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
                this.setConsumerFactory(kafkaConsumerFactory)
            }
        }
    }
}