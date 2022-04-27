package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.util.ReflectionTestUtils
import java.io.File

private const val TOPIC = "test"

private const val NUM_BROKERS = 1

private const val CONTROLLED_BROKER_SHUTDOWN = true

/**
 * "Strikk og binders"-test for manuell testing av PDF-generering. Denne testen bør ikke inngå i vanlig testkjøring av
 * integrasjonstester, men kan være nyttig under utvikling og feilretting.
 *
 * Forutsetninger:
 * Lokal kjøring av <code>yrkesskade-dokgen</code> må være startet, eller overstyr til å bruke testmiljøet.
 *
 * Kommenter ut @Disabled annoteringer for å kjøre testene.
 *
 * OBS! @Disabled annoteringene blir ikke alltid tatt hensyn til i IntelliJ.
 */
@Disabled("Disabled ved automatisk testkjøring")
@Suppress("NonAsciiCharacters")
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
    fun `skademelding - A) tro-kopi-pdf`() {
        val byteArray = pdfService.lagPdf(enkelSkademeldingInnsendtHendelse(), PdfTemplate.SKADEMELDING_TRO_KOPI)
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skademelding_tro-kopi.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `skademelding - B) saksbehandling-pdf`() {
        val byteArray = pdfService.lagBeriketPdf(enkelSkademeldingInnsendtHendelse(), beriketData(), PdfTemplate.SKADEMELDING_SAKSBEHANDLING)
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skademelding_saksbehandling.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `skadeforklaring - 1A) tro-kopi-pdf - foresatt melder på vegne av barn`() {
        val byteArray = pdfService.lagPdf(
            enkelSkadeforklaringInnsendingHendelse(),
            PdfTemplate.SKADEFORKLARING_TRO_KOPI
        )
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skadeforklaring_tro-kopi_foresatt-melder.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `skadeforklaring - 1B) tro-kopi-pdf - skadelidt melder selv`() {
        val byteArray = pdfService.lagPdf(
            enkelSkadeforklaringInnsendingHendelseHvorSkadelidtMelderSelv(),
            PdfTemplate.SKADEFORKLARING_TRO_KOPI
        )
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skadeforklaring_tro-kopi_skadelidt-melder.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `skadeforklaring - 2A) beriket pdf - foresatt melder på vegne av barn`() {
        val byteArray = pdfService.lagBeriketPdf(
            enkelSkadeforklaringInnsendingHendelseMedVedlegg(),
            beriketData(),
            PdfTemplate.SKADEFORKLARING_BERIKET
        )
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skadeforklaring_beriket_foresatt-melder.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `skadeforklaring - 2B) beriket pdf - skadelidt melder selv`() {
        val byteArray = pdfService.lagBeriketPdf(
            enkelSkadeforklaringInnsendingHendelseHvorSkadelidtMelderSelv(),
            beriketData(), PdfTemplate.SKADEFORKLARING_BERIKET
        )
        println("Pdf-størrelsen er ${byteArray.size} bytes")

        File("Skadeforklaring_beriket_skadelidt-melder.pdf").writeBytes(byteArray)
        println("Ferdig med å lage pdf.")
    }

    @Test
    fun `kodeverk landkoder`() {
        val map = ReflectionTestUtils.invokeMethod<Map<KodeverkKode, KodeverkVerdi>>(pdfService, "landkoder", "nb")!!
        assertThat(map.size).isGreaterThan(0)
        val norge = map["NOR"]!!
        assertThat(norge.kode).isEqualTo("NOR")
        assertThat(norge.verdi).isEqualTo("NORGE")
    }

    @Test
    fun `kodeverk fravaertyper`() {
        val map = ReflectionTestUtils.invokeMethod<Map<KodeverkKode, KodeverkVerdi>>(pdfService, "fravaertyper", "nb")!!
        assertThat(map.size).isEqualTo(4)
        assertThat(map.keys).containsExactlyInAnyOrder(
            "sykemelding",
            "egenmelding",
            "kombinasjonSykemeldingEgenmelding",
            "alternativenePasserIkke"
        )
        val egenmelding = map["egenmelding"]!!
        assertThat(egenmelding.kode).isEqualTo("egenmelding")
        assertThat(egenmelding.verdi).isEqualTo("Egenmelding")
    }

}