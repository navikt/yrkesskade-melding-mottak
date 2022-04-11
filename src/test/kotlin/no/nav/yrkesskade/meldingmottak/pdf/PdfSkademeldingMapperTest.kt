package no.nav.yrkesskade.meldingmottak.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.fixtures.beriketData
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkademeldingInnsendtHendelse
import no.nav.yrkesskade.meldingmottak.fixtures.noenLand
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.*
import no.nav.yrkesskade.skademelding.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PdfSkademeldingMapperTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())


    @Test
    fun `skal mappe skademelding til PdfSkademeldig`() {
        val record = enkelSkademeldingInnsendtHendelse()
        println("skademeldingen er:\n $record")
        val beriketData = beriketData()
        println("beriket data er:\n $beriketData")

        val pdfSkademelding = PdfSkademeldingMapper.tilPdfSkademelding(record, noenLand(), beriketData)
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
        assertThat(innmelder?.innmelderrolle?.verdi).isEqualTo(Innmelderrolle.virksomhetsrepresentant.value)
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
        assertThat(skadelidt?.dekningsforhold?.stillingstittelTilDenSkadelidte?.verdi).containsExactlyInAnyOrder(
            Stillingstittel.altmuligmann.value,
            Stillingstittel.agroteknikere.value
        )
        assertThat(skadelidt?.dekningsforhold?.rolletype?.verdi).isEqualTo(Rolletype.arbeidstaker.value)
    }

    private fun assertSkade(skade: PdfSkade?) {
        assertThat(skade?.alvorlighetsgrad?.verdi).isEqualTo(Alvorlighetsgrad.andreLivstruendeSykdomSlashSkade.value)
        assertThat(skade?.skadedeDeler).containsExactlyInAnyOrder(
            PdfSkadetDel(
                kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", KroppsdelTabellD.ansikt.value),
                skadeartTabellC = Soknadsfelt("Hva slags skade er det", SkadeartTabellC.etsing.value)
            ),
            PdfSkadetDel(
                kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", KroppsdelTabellD.armSlashAlbueCommaVenstre.value),
                skadeartTabellC = Soknadsfelt("Hva slags skade er det", SkadeartTabellC.knokkelbrudd.value)
            )
        )
        assertThat(skade?.antattSykefravaerTabellH?.verdi).isEqualTo(AntattSykefravaerTabellH.kjentFravRMerEnn3Dager.value)
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
        assertThat(hendelsesfakta?.naarSkjeddeUlykken?.verdi).isEqualTo(NaarSkjeddeUlykken.iAvtaltArbeidstid.value)
        assertThat(hendelsesfakta?.hvorSkjeddeUlykken?.verdi).isEqualTo(HvorSkjeddeUlykken.pArbeidsstedetUte.value)
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
            UlykkesAarsakTabellAogE.kjemikalier.value,
            UlykkesAarsakTabellAogE.fallAvPerson.value
        )
        assertThat(hendelsesfakta?.bakgrunnsaarsakTabellBogG?.verdi).containsExactlyInAnyOrder(
            BakgrunnsaarsakTabellBogG.defektUtstyr.value,
            BakgrunnsaarsakTabellBogG.feilPlassering.value,
            BakgrunnsaarsakTabellBogG.mangelfullOpplRing.value
        )
        assertThat(hendelsesfakta?.stedsbeskrivelseTabellF?.verdi).isEqualTo(StedsbeskrivelseTabellF.plassForIndustriellVirksomhet.value)
        assertThat(hendelsesfakta?.utfyllendeBeskrivelse?.verdi).contains("blabla bla ")
    }

    private fun assertDokumentInfo(dokumentInfo: PdfDokumentInfo) {
        assertThat(dokumentInfo.dokumentnavn).isEqualTo("Melding om yrkesskade eller yrkessykdom")
        assertThat(dokumentInfo.dokumentnummer).isEqualTo("NAV 13")
        assertThat(dokumentInfo.dokumentDatoPrefix).isEqualTo("Innsendt digitalt ")
        assertThat(dokumentInfo.dokumentDato).isEqualTo("28.02.2022")
    }
}