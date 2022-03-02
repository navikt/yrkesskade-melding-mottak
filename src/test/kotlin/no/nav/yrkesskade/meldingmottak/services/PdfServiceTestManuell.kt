package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import java.io.File

private const val TOPIC = "test"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

/**
 * "Strikk og binders"-test for manuell testing av PDF-generering. Denne testen bør ikke inngå i vanlig testkjøring av
 * integrasjonstester, men kan være nyttig under utvikling og feilretting.
 *
 * <h3>Forutsetninger</h3>
 * Lokal kjøring av <code>yrkesskade-dokgen</code> må være startet.
 *
 */
@EmbeddedKafka(topics = [TOPIC])
internal class PdfServiceTestManuell : BaseSpringBootTestClass() {

    @Autowired
    lateinit var pdfService: PdfService


    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    val embeddedKafkaBroker = EmbeddedKafkaBroker(NUM_BROKERS, CONTROLLED_BROKER_SHUTDOWN, TOPIC)

    @BeforeEach
    fun setupBeforeEachTest() {
        for (messageListenerContainer in kafkaListenerEndpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.partitionsPerTopic
            )
        }
    }

    @Test
    fun `skal lage pdf`() {
        val byteArray = pdfService.lagPdf(skademeldingInnsendtHendelse(), PdfTemplate.SKADEMELDING)

        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("tro-kopi-test.pdf").writeBytes(byteArray)

        println("Ferdig med å lage pdf.")
    }

}