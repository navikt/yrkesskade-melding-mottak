package no.nav.yrkesskade.meldingmottak.hendelser

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Before
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.condition.EmbeddedKafkaCondition
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka(partitions = 1, ports = [9092], bootstrapServersProperty = "spring.kafka.bootstrap-servers")
internal class JournalfoeringHendelseConsumerTest {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val topicName: String = "test"

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    init {
        PostgresDockerContainer.container
    }

    @Before
    fun init() {
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, EmbeddedKafkaCondition.getBroker().partitionsPerTopic)
        }
    }

    @Test
    fun listen() {
        kafkaTemplate.send(topicName, record()).get()
    }

    fun record(): JournalfoeringHendelseRecord? {
        return JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("hendelsesId")
                .setVersjon(1)
                .setHendelsesType("hendelsesType")
                .setJournalpostId(1337)
                .setJournalpostStatus("journalpostStatus")
                .setTemaGammelt("YRK")
                .setTemaNytt("YRK")
                .setMottaksKanal("NRK")
                .setKanalReferanseId("P1")
                .setBehandlingstema("YRK")
                .build()
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

        @Bean
        fun kafkaProducerFactory(properties: KafkaProperties,
                                 schemaRegistryClient: MockSchemaRegistryClient): ProducerFactory<String, Any> {
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
}