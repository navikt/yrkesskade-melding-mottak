package no.nav.yrkesskade.meldingmottak.hendelser

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.timeout
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val TOPICS = "test"

@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka(topics = [TOPICS])
internal class JournalfoeringHendelseConsumerTest {

    @Captor
    lateinit var argumentCaptor: ArgumentCaptor<JournalfoeringHendelseRecord>

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @SpyBean
    lateinit var consumer: JournalfoeringHendelseConsumer

    val topicName: String = "test"

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
        val record = record()
//        val latch = CountDownLatch(1)
        kafkaTemplate.send(topicName, record).get()
//        latch.await(5, TimeUnit.SECONDS)
//        latch.countDown()

        Mockito.verify(consumer, timeout(5000).times(1))
                .listen(argumentCaptor.capture())
        Assertions.assertThat(argumentCaptor.value).isEqualTo(record)
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