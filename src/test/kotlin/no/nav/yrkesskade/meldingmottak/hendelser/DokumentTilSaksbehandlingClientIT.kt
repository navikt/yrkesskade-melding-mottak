package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.dokumentTilSaksbehandlingHendelse
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingHendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.stereotype.Component

private const val TOPIC = "yrkesskade.privat-yrkesskade-dokument-til-saksbehandling"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

@EmbeddedKafka(topics = [TOPIC])
class DokumentTilSaksbehandlingClientIT : BaseSpringBootTestClass() {

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @Autowired
    private lateinit var dokumentTilSaksbehandlingClient: DokumentTilSaksbehandlingClient

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @SpyBean
    private lateinit var dokumentTilSaksbehandlingConsumer: DokumentTilSaksbehandlingConsumer

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
    fun `send melding til saksbehandling`() {
        val dokumentTilSaksbehandlingHendelse = dokumentTilSaksbehandlingHendelse()
        dokumentTilSaksbehandlingClient.sendTilSaksbehandling(dokumentTilSaksbehandlingHendelse)
        Mockito.verify(dokumentTilSaksbehandlingConsumer, timeout(20000L).times(1)).receive(any())

        assertThat(dokumentTilSaksbehandlingConsumer.getPayload()).isEqualTo(dokumentTilSaksbehandlingHendelse)
    }
}


@Component
class DokumentTilSaksbehandlingConsumer {

    private lateinit var payload: DokumentTilSaksbehandlingHendelse

    @KafkaListener(
        topics = ["\${kafka.topic.dokument-til-saksbehandling}"],
        containerFactory = "dokumentTilSaksbehandlingHendelseListenerContainerFactory",
        id = "dokument-til-saksbehandling",
        idIsGroup = false
    )
    fun receive(record: DokumentTilSaksbehandlingHendelse) {
        payload = record
    }

    fun getPayload() = payload
}