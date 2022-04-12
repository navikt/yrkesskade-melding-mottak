package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.integration.model.*
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringMetadata
import no.nav.yrkesskade.meldingmottak.pdf.domene.*

object PdfSkadeforklaringMapper {

    fun tilPdfSkadeforklaring(
        record: SkadeforklaringInnsendingHendelse,
        fravaertyper: Map<KodeverkKode, KodeverkVerdi>,
        beriketData: BeriketData? = null
    ) : PdfSkadeforklaring {

        val skadeforklaring = record.skadeforklaring
        val innmelder = tilPdfInnmelder(skadeforklaring.innmelder, beriketData?.innmeldersNavn)
        val skadelidt = tilPdfSkadelidt(skadeforklaring.skadelidt, beriketData?.skadelidtsNavn)
        val tid = tilPdfTid(skadeforklaring)
        val arbeidsbeskrivelse = tilPdfArbeidsbeskrivelse(skadeforklaring.arbeidsbeskrivelse)
        val ulykkesbeskrivelse = tilPdfUlykkesbeskrivelse(skadeforklaring.ulykkesbeskrivelse)
        val fravaer = tilPdfFravaer(skadeforklaring.fravaer, fravaertyper)
        val behandler = tilPdfBehandler(skadeforklaring.behandler)
        val dokumentInfo = lagPdfDokumentInfo(record.metadata)

        return PdfSkadeforklaring(
            innmelder = innmelder,
            skadelidt = skadelidt,
            tid = tid,
            arbeidsbeskrivelse = arbeidsbeskrivelse,
            ulykkesbeskrivelse = ulykkesbeskrivelse,
            fravaer = fravaer,
            behandler = behandler,
           dokumentInfo = dokumentInfo
        )
    }

    private fun tilPdfInnmelder(innmelder: Innmelder, innmeldersNavn: Navn?): PdfInnmelder {
        return PdfInnmelder(
            norskIdentitetsnummer = Soknadsfelt("Fødselsnummer", innmelder.norskIdentitetsnummer),
            navn = Soknadsfelt("Navn", innmeldersNavn?.toString().orEmpty()),
            innmelderrolle = Soknadsfelt("Rolle", innmelder.rolle)
        )
    }

    private fun tilPdfSkadelidt(skadelidt: Skadelidt, skadelidtsNavn: Navn?): PdfSkadelidt {
        return PdfSkadelidt(
            Soknadsfelt("Fødselsnummer", skadelidt.norskIdentitetsnummer),
            Soknadsfelt("Navn", skadelidtsNavn?.toString().orEmpty())
        )
    }

    private fun tilPdfTid(skadeforklaring: Skadeforklaring): PdfTid {
        return PdfTid(
            tidstype = skadeforklaring.tid.tidstype,
            tidspunkt = Soknadsfelt(
                "Når skjedde ulykken?",
                PdfTidspunkt(
                    dato = MapperUtil.datoFormatert(skadeforklaring.tid.tidspunkt),
                    klokkeslett = MapperUtil.klokkeslettFormatert(skadeforklaring.tid.tidspunkt)
                )
            ),
            periode = Soknadsfelt(
                "Når skjedde ulykken?",
                PdfPeriode(
                    fra = MapperUtil.datoFormatert(skadeforklaring.tid.periode?.fra),
                    til = MapperUtil.datoFormatert(skadeforklaring.tid.periode?.til)
                )
            ),
            ukjent = Soknadsfelt("", null)
        )
    }

    private fun tilPdfArbeidsbeskrivelse(arbeidsbeskrivelse: String): Soknadsfelt<String> =
        Soknadsfelt("Hva arbeidet du med i ulykkesøyeblikket?", arbeidsbeskrivelse)

    private fun tilPdfUlykkesbeskrivelse(ulykkesbeskrivelse: String): Soknadsfelt<String> =
        Soknadsfelt("Gi en så nøyaktig beskrivelse av hendelsen som mulig", ulykkesbeskrivelse)

    private fun tilPdfFravaer(fravaer: Fravaer, fravaertyper: Map<KodeverkKode, KodeverkVerdi>): PdfFravaer {
        return PdfFravaer(
            Soknadsfelt("Førte din skade/sykdom til fravær?", MapperUtil.jaNei(fravaer.harFravaer)),
            Soknadsfelt("Velg type fravær", hentVerdi(fravaer.fravaertype, fravaertyper))
        )
    }

    private fun hentVerdi(kode: KodeverkKode, kodeverdier: Map<KodeverkKode, KodeverkVerdi>): String {
        return kodeverdier[kode]?.verdi.orEmpty()
    }

    private fun tilPdfBehandler(behandler: Behandler): PdfBehandler {
        return PdfBehandler(
            erBehandlerOppsokt = Soknadsfelt("Ble lege oppsøkt etter skaden?", MapperUtil.jaNei(behandler.erBehandlerOppsokt)),
            behandlernavn = Soknadsfelt("Navn på helseforetak, legevakt eller lege", behandler.behandlerNavn),
            behandleradresse = Soknadsfelt("Adresse", tilPdfAdresse(behandler.adresse))
        )
    }

    private fun tilPdfAdresse(adresse: Adresse?): PdfAdresse? {
        if (adresse == null) {
            return null
        }
        return PdfAdresse(
            adresselinje1 = adresse.adresse,
            adresselinje2 = adresse.postnummer + " " + adresse.poststed,
            adresselinje3 = null,
            land = null
        )
    }

    private fun lagPdfDokumentInfo(metadata: SkadeforklaringMetadata): PdfDokumentInfo {
        return PdfDokumentInfo(
            dokumentnavn = "Skadeforklaring ved arbeidsulykke",
            dokumentnummer = "NAV 13-00.21",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = MapperUtil.datoFormatert(metadata.tidspunktMottatt),
            tekster = lagPdfTekster()
        )
    }

    private fun lagPdfTekster(): PdfTekster {
        return PdfTekster(
            innmelderSeksjonstittel = "Om innmelder",
            skadelidtSeksjonstittel = "Den skadelidte",
            tidOgStedSeksjonstittel = "Tid og sted",
            omUlykkenSeksjonstittel = "Om ulykken",
            omSkadenSeksjonstittel = "Om skaden",
            omSkadenFlereSkader = ""
        )
    }

}