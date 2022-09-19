package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.skadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.services.SkadeforklaringService
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringMetadata as SkadeforklaringMetadataV2
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.Spraak
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV2
import no.nav.yrkesskade.skadeforklaring.v2.model.SkadeforklaringFactory
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
import java.time.Instant

private const val TOPIC = "yrkesskade.privat-yrkesskade-skadeforklaringinnsendt"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

@EmbeddedKafka(topics = [TOPIC])
internal class SkadeforklaringInnsendingHendelseConsumerIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @SpyBean
    lateinit var consumer: SkadeforklaringInnsendingHendelseConsumer

    @SpyBean
    lateinit var skadeforklaringService: SkadeforklaringService

    @Autowired
    lateinit var skadeforklaringKafkaTemplate: KafkaTemplate<String, Any>

    @BeforeEach
    fun init() {
        doNothing().`when`(skadeforklaringService).mottaSkadeforklaring(any())
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.partitionsPerTopic
            )
        }
    }

    @Test
    fun handleSkadeforklaringV1() {
        val record = skadeforklaringInnsendingHendelse()
        skadeforklaringKafkaTemplate.send(TOPIC, record)
        Mockito.verify(consumer, timeout(20000L).times(1)).handleSkadeforklaringV1(any())
        Mockito.verify(skadeforklaringService, timeout(20000L).times(1)).mottaSkadeforklaring(eq(record))
    }

    @Test
    fun handleSkadeforklaringV2() {
        val record = SkadeforklaringInnsendingHendelseV2(
            SkadeforklaringMetadataV2(Instant.now(), Spraak.NB, "test-${Instant.now().epochSecond}"),
            SkadeforklaringFactory.enSkadeforklaring()
        )
        skadeforklaringKafkaTemplate.send(TOPIC, record)
        Mockito.verify(consumer, timeout(20000L).times(1)).handleSkadeforklaringV2(any())
        Mockito.verify(skadeforklaringService, timeout(20000L).times(1)).mottaSkadeforklaring(eq(record))
    }
}
