package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.skadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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

private const val TOPIC = "yrkesskade.privat-yrkesskade-skadeforklaringinnsendt"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

@Disabled("Disabled ved automatisk testkjøring")
@EmbeddedKafka(topics = [TOPIC])
internal class SkadeforklaringInnsendingHendelseConsumerIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @SpyBean
    lateinit var consumer: SkadeforklaringInnsendingHendelseConsumer

    @Autowired
    lateinit var skadeforklaringKafkaTemplate: KafkaTemplate<String, SkadeforklaringInnsendingHendelse>

    @BeforeEach
    fun init() {
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.partitionsPerTopic
            )
        }
    }

    @Disabled("Disabled ved automatisk testkjøring")
    @Test
    fun listen() {
        val record = skadeforklaringInnsendingHendelse()
        skadeforklaringKafkaTemplate.send(TOPIC, record)
        Mockito.verify(consumer, timeout(20000L).times(1)).listen(any())
    }

}
