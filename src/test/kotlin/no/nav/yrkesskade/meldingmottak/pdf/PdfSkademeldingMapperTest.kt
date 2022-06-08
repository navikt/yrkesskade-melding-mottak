package no.nav.yrkesskade.meldingmottak.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.fixtures.*
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.*
import no.nav.yrkesskade.meldingmottak.services.KodeverkService
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.skademelding.model.Tidstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@Suppress("UNUSED_VARIABLE")
internal class PdfSkademeldingMapperTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private val kodeverkService = mock(KodeverkService::class.java)

    @BeforeEach
    fun setUp() {
        `when`(kodeverkService.hentKodeverk(eq("landkoder"), eq(null), any())).thenReturn(noenLand())
        `when`(kodeverkService.hentKodeverk(eq("fravaertype"), eq(null), any())).thenReturn(fravaertyper())
        `when`(kodeverkService.hentKodeverk(eq("rolletype"), eq(null), any())).thenReturn(
            rolletyper()
        )
        `when`(kodeverkService.hentKodeverk(eq("tidsrom"), any(), any())).thenReturn(
            tidsrom()
        )
        `when`(kodeverkService.hentKodeverk(eq("stillingstittel"), any(), any())).thenReturn(stillingstitler())
        `when`(kodeverkService.hentKodeverk(eq("harSkadelidtHattFravaer"), any(), any())).thenReturn(
            harSkadelidtHattFravaer()
        )
        `when`(kodeverkService.hentKodeverk(eq("hvorSkjeddeUlykken"), any(), any())).thenReturn(hvorSkjeddeUlykken())
        `when`(kodeverkService.hentKodeverk(eq("typeArbeidsplass"), any(), any())).thenReturn(typeArbeidsplass())
        `when`(kodeverkService.hentKodeverk(eq("skadetype"), any(), any())).thenReturn(skadetyper())
        `when`(kodeverkService.hentKodeverk(eq("skadetKroppsdel"), any(), any())).thenReturn(skadetKroppsdel())
        `when`(kodeverkService.hentKodeverk(eq("bakgrunnForHendelsen"), any(), any())).thenReturn(bakgrunnForHendelsen())
        `when`(kodeverkService.hentKodeverk(eq("aarsakOgBakgrunn"), any(), any())).thenReturn(aarsakBakgrunn())
        `when`(kodeverkService.hentKodeverk(eq("alvorlighetsgrad"), any(), any())).thenReturn(alvorlighetsgrad())
    }

    @Test
    fun `skal mappe skademelding til PdfSkademelding`() {
        val kodeverkHolder = KodeverkHolder.init("arbeidstaker", kodeverkService)
        val record = enkelSkademeldingInnsendtHendelse()
        println("skademeldingen er:\n $record")
        val beriketData = beriketData()
        println("beriket data er:\n $beriketData")

        val pdfSkademelding = PdfSkademeldingMapper.tilPdfSkademelding(record, kodeverkHolder, beriketData)
        println("PdfSkademeldingen er $pdfSkademelding")

        assertPdfSkademelding(pdfSkademelding)

        val prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pdfSkademelding)
        //println("Pretty JSON er \n$prettyJson") // Kommenter inn ved behov
    }

    private fun assertPdfSkademelding(skademelding: PdfSkademelding) {
        assertThat(skademelding).isNotNull
        assertInnmelder(skademelding.innmelder)
        assertSkadelidt(skademelding.skadelidt)
        assertSkade(skademelding.skade)
        assertHendelsesfakta(skademelding.hendelsesfakta)
        assertDokumentInfo(skademelding.dokumentInfo)
    }

    private fun assertInnmelder(innmelder: PdfInnmelder?) {
        assertThat(innmelder).isNotNull
        assertThat(innmelder?.norskIdentitetsnummer?.verdi).isEqualTo("12345677777")
        assertThat(innmelder?.navn?.verdi).isEqualTo("Inn Melder")
        assertThat(innmelder?.paaVegneAv?.verdi).isEqualTo("123454321")
        assertThat(innmelder?.innmelderrolle?.verdi).isEqualTo("virksomhetsrepresentant")
        assertThat(innmelder?.altinnrolleIDer?.verdi).isEqualTo(listOf("111", "22"))
    }

    private fun assertSkadelidt(skadelidt: PdfSkadelidt?) {
        assertThat(skadelidt?.norskIdentitetsnummer?.verdi).isEqualTo("11111177777")
        assertThat(skadelidt?.navn?.verdi).isEqualTo("Ska De Lidt")
        assertThat(skadelidt?.bostedsadresse?.verdi).isEqualTo(
            PdfAdresse(
                adresselinje1 = "Stigen 7A",
                adresselinje2 = "7730 Småby",
                adresselinje3 = null,
                land = null
            ))
        assertThat(skadelidt?.dekningsforhold?.organisasjonsnummer?.verdi).isEqualTo("123456789")
        assertThat(skadelidt?.dekningsforhold?.navnPaaVirksomheten?.verdi).isEqualTo("Bedriften AS")
        assertThat(skadelidt?.dekningsforhold?.virksomhetensAdresse?.verdi).isEqualTo(
            PdfAdresse(
                adresselinje1 = "Virksomhetsgata 70",
                adresselinje2 = "9955 Industribyen",
                adresselinje3 = null,
                land = "SVERIGE"
            ))
        assertThat(skadelidt?.dekningsforhold?.stillingstittelTilDenSkadelidte?.verdi).containsExactlyInAnyOrder(
            "Altmuligmann",
            "Agroteknikere"
        )
        assertThat(skadelidt?.dekningsforhold?.rolletype?.verdi).isEqualTo(
            PdfRolletype("arbeidstaker", "Arbeidstaker")
        )
    }

    private fun assertSkade(skade: PdfSkade?) {
        assertThat(skade?.alvorlighetsgrad?.verdi).isEqualTo("Livstruende sykdom/skade")
        assertThat(skade?.skadedeDeler).containsExactlyInAnyOrder(
            PdfSkadetDel(
                kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", "Ansikt"),
                skadeartTabellC = Soknadsfelt("Hva slags skade er det", "Etsing")
            ),
            PdfSkadetDel(
                kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", "Arm/albue, venstre"),
                skadeartTabellC = Soknadsfelt("Hva slags skade er det", "Bruddskade")
            )
        )
        assertThat(skade?.antattSykefravaerTabellH?.verdi).isEqualTo("Kjent fravær mer enn 3 dager")
    }

    private fun assertHendelsesfakta(hendelsesfakta: PdfHendelsesfakta?) {
        assertThat(hendelsesfakta?.tid?.tidstype).isEqualTo(Tidstype.tidspunkt.value)
        assertThat(hendelsesfakta?.tid?.tidspunkt?.verdi).isEqualTo(
            PdfTidspunkt(
                dato = "28.02.2022",
                klokkeslett = "17.15"
            )
        )
        assertThat(hendelsesfakta?.tid?.periode).isEqualTo(Soknadsfelt(
            "Når skjedde ulykken som skal meldes?",
            PdfPeriode("", "")
        ))
        assertThat(hendelsesfakta?.tid?.ukjent?.verdi).isFalse
        assertThat(hendelsesfakta?.naarSkjeddeUlykken?.verdi).isEqualTo("I avtalt arbeidstid")
        assertThat(hendelsesfakta?.hvorSkjeddeUlykken?.verdi).isEqualTo("På arbeidsstedet ute")
        assertThat(hendelsesfakta?.ulykkessted?.sammeSomVirksomhetensAdresse?.verdi).isEqualTo("Ja")
        assertThat(hendelsesfakta?.ulykkessted?.adresse?.verdi).isEqualTo(
            PdfAdresse(
                adresselinje1 = "Storgaten 13",
                adresselinje2 = "2345 Småbygda",
                adresselinje3 = null,
                land = "SVERIGE"
            )
        )
        assertThat(hendelsesfakta?.aarsakUlykkeTabellAogE?.verdi).containsExactlyInAnyOrder(
            "Velt",
            "Fall av person"
        )
        assertThat(hendelsesfakta?.bakgrunnsaarsakTabellBogG?.verdi).containsExactlyInAnyOrder(
            "Defekt utstyr",
            "Feil plassering",
            "Mangelfull opplæring"
        )
        assertThat(hendelsesfakta?.stedsbeskrivelseTabellF?.verdi).isEqualTo("Plass for industriell virksomhet")
        assertThat(hendelsesfakta?.utfyllendeBeskrivelse?.verdi).contains("blabla bla ")
    }

    private fun assertDokumentInfo(dokumentInfo: PdfDokumentInfo) {
        assertThat(dokumentInfo.dokumentnavn).isEqualTo("Melding om yrkesskade eller yrkessykdom")
        assertThat(dokumentInfo.dokumentnummer).isEqualTo("NAV 13")
        assertThat(dokumentInfo.dokumentDatoPrefix).isEqualTo("Innsendt digitalt ")
        assertThat(dokumentInfo.dokumentDato).isEqualTo("28.02.2022")
    }
}