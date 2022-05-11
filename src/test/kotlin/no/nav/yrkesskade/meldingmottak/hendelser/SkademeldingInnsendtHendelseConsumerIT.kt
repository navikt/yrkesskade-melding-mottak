package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import no.nav.yrkesskade.meldingmottak.services.SkademeldingService
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils

private const val TOPIC = "yrkesskade.privat-yrkesskade-skademeldinginnsendt"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

@EmbeddedKafka(topics = [TOPIC])
internal class SkademeldingInnsendtHendelseConsumerIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @SpyBean
    lateinit var consumer: SkademeldingInnsendtHendelseConsumer

    @SpyBean
    lateinit var skademeldingService: SkademeldingService

    @Autowired
    lateinit var skademeldingKafkaTemplate: KafkaTemplate<String, SkademeldingInnsendtHendelse>

    @BeforeEach
    fun init() {
        doNothing().`when`(skademeldingService).mottaSkademelding(any())
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.partitionsPerTopic
            )
        }
    }

    @Test
    fun listen() {
        val record = skademeldingInnsendtHendelse()
        skademeldingKafkaTemplate.send(TOPIC, record)
        Mockito.verify(consumer, timeout(20000L).times(1)).listen(any())
        Mockito.verify(skademeldingService, timeout(20000L).times(1)).mottaSkademelding(eq(record))
    }
}