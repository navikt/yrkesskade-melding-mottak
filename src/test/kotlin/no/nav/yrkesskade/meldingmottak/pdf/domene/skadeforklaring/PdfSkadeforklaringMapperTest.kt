package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.fixtures.beriketData
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.fixtures.fravaertyper
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.skademelding.model.Tidstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PdfSkadeforklaringMapperTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())


    @Test
    fun `skal mappe skadeforklaring til PdfSkadeforklaring`() {
        val record = enkelSkadeforklaringInnsendingHendelse()
        println("skadeforklaringen er:\n $record")
        val beriketData = beriketData()
        println("beriket data er:\n $beriketData")

        val pdfSkadeforklaring = PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record, fravaertyper(), beriketData)
        println("PdfSkadeforklaringen er $pdfSkadeforklaring")

        assertPdfSkadeforklaring(pdfSkadeforklaring)

        val prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pdfSkadeforklaring)
        //println("Pretty JSON er \n$prettyJson") // Kommenter inn ved behov
    }

    private fun assertPdfSkadeforklaring(skadeforklaring: PdfSkadeforklaring) {
        assertThat(skadeforklaring).isNotNull
        assertInnmelder(skadeforklaring.innmelder)
        assertSkadelidt(skadeforklaring.skadelidt)
        assertTid(skadeforklaring.tid)
        assertArbeidsbeskrivelse(skadeforklaring.arbeidetMedIUlykkesoeyeblikket)
        assertUlykkesbeskrivelse(skadeforklaring.noeyaktigBeskrivelseAvHendelsen)
        assertFravaer(skadeforklaring.fravaer)
        assertHelseinstitusjon(skadeforklaring.helseinstitusjon)
        assertDokumentInfo(skadeforklaring.dokumentInfo)
    }

    private fun assertInnmelder(innmelder: PdfInnmelder) {
        assertThat(innmelder).isNotNull
        assertThat(innmelder.norskIdentitetsnummer.verdi).isEqualTo("12345600000")
        assertThat(innmelder.navn.verdi).isEqualTo("Inn Melder")
        assertThat(innmelder.innmelderrolle.verdi).isEqualTo("Verge/Foresatt")
    }

    private fun assertSkadelidt(skadelidt: PdfSkadelidt?) {
        assertThat(skadelidt?.norskIdentitetsnummer?.verdi).isEqualTo("12120522222")
        assertThat(skadelidt?.navn?.verdi).isEqualTo("Ska De Lidt")
    }

    private fun assertTid(tid: PdfTid) {
        assertThat(tid.tidstype).isEqualTo(Tidstype.tidspunkt.value.uppercase())
        assertThat(tid.tidspunkt.verdi).isEqualTo(
            PdfTidspunkt(
                dato = "10.04.2022",
                klokkeslett = "16.03"
            )
        )
        assertThat(tid.periode).isEqualTo(
            Soknadsfelt(
                "Når skjedde ulykken?",
                PdfPeriode("", "")
            )
        )
        assertThat(tid.ukjent.verdi).isNull()
    }

    private fun assertArbeidsbeskrivelse(arbeidsbeskrivelse: Soknadsfelt<String>) {
        assertThat(arbeidsbeskrivelse.verdi).isEqualTo("Dette er arbeidsbeskrivelsen")
    }

    private fun assertUlykkesbeskrivelse(ulykkesbeskrivelse: Soknadsfelt<String>) {
        assertThat(ulykkesbeskrivelse.verdi).isEqualTo("Dette er ulykkesbeskrivelsen")
    }

    private fun assertFravaer(fravaer: PdfFravaer) {
        assertThat(fravaer.foerteDinSkadeEllerSykdomTilFravaer.verdi).isEqualTo("Ja")
        assertThat(fravaer.fravaertype.verdi).isEqualTo("Sykemelding")
    }

    private fun assertHelseinstitusjon(helseinstitusjon: PdfHelseinstitusjon) {
        assertThat(helseinstitusjon.erHelsepersonellOppsokt.verdi).isEqualTo("Ja")
        assertThat(helseinstitusjon.navn.verdi).isEqualTo("Bli-bra-igjen Legesenter")
        assertThat(helseinstitusjon.adresse.verdi).isEqualTo(
            PdfAdresse(
                adresselinje1 = "Stien 3B",
                adresselinje2 = "1739 Granlia",
                adresselinje3 = null,
                land = null
            )
        )
    }

    private fun assertDokumentInfo(dokumentInfo: PdfDokumentInfoSkadeforklaring) {
        assertThat(dokumentInfo.dokumentnavn).isEqualTo("Skadeforklaring ved arbeidsulykke")
        assertThat(dokumentInfo.dokumentnummer).isEqualTo("NAV 13-00.21")
        assertThat(dokumentInfo.dokumentDatoPrefix).isEqualTo("Innsendt digitalt ")
        assertThat(dokumentInfo.dokumentDato).isEqualTo("08.04.2022")
    }

}