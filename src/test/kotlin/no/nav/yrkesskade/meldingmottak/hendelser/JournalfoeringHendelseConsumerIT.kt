package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils

private const val TOPIC = "test"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

@EmbeddedKafka(topics = [TOPIC])
internal class JournalfoeringHendelseConsumerIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @SpyBean
    lateinit var consumer: JournalfoeringHendelseConsumer

    @Autowired
    lateinit var journalfoeringHendelseKafkaTemplate: KafkaTemplate<String, Any>

    @BeforeEach
    fun init() {
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.partitionsPerTopic
            )
        }
    }

    @Test
    fun listen() {
        val record = journalfoeringHendelseRecord()
        journalfoeringHendelseKafkaTemplate.send(TOPIC, record).get()
        Mockito.verify(consumer, timeout(20000L).times(1)).listen(any())
    }
}