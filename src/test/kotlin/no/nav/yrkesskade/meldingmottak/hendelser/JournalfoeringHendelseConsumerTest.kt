package no.nav.yrkesskade.meldingmottak.hendelser

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Before
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.timeout
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
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

private const val TOPIC = "test"

@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka(topics = [TOPIC])
internal class JournalfoeringHendelseConsumerTest {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @SpyBean
    lateinit var consumer: JournalfoeringHendelseConsumer

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    init {
        PostgresDockerContainer.container
    }

    @Before
    fun init() {
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                    messageListenerContainer,
                    EmbeddedKafkaCondition.getBroker().partitionsPerTopic
            )
        }
    }

    @Test
    fun listen() {
        val record = journalfoeringHendelseRecord()
        kafkaTemplate.send(TOPIC, record).get()
        Mockito.verify(consumer, timeout(20000L).times(1)).listen(any())
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