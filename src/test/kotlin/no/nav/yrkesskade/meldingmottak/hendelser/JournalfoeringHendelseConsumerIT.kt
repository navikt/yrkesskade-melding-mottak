package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalfoeringHendelseRecord
import org.junit.Before
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.timeout
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.condition.EmbeddedKafkaCondition
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils

private const val TOPIC = "test"

@EmbeddedKafka(topics = [TOPIC])
internal class JournalfoeringHendelseConsumerIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @SpyBean
    lateinit var consumer: JournalfoeringHendelseConsumer

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, Any>

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
}