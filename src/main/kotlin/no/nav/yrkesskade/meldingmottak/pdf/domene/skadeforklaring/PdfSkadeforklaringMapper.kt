package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringMetadata
import no.nav.yrkesskade.skadeforklaring.model.*

object PdfSkadeforklaringMapper {

    fun tilPdfSkadeforklaring(
        record: SkadeforklaringInnsendingHendelse,
        kodeverkHolder: KodeverkHolder,
        beriketData: BeriketData? = null
    ) : PdfSkadeforklaring {

        val skadeforklaring = record.skadeforklaring
        val innmelder = tilPdfInnmelder(skadeforklaring.innmelder, beriketData?.innmeldersNavn, kodeverkHolder)
        val skadelidt = tilPdfSkadelidt(skadeforklaring.skadelidt, beriketData?.skadelidtsNavn)
        val tid = tilPdfTid(skadeforklaring)
        val arbeidsbeskrivelse = tilPdfArbeidsbeskrivelse(skadeforklaring.arbeidetMedIUlykkesoeyeblikket)
        val ulykkesbeskrivelse = tilPdfUlykkesbeskrivelse(skadeforklaring.noeyaktigBeskrivelseAvHendelsen)
        val fravaer = tilPdfFravaer(skadeforklaring.fravaer, kodeverkHolder)
        val helseinstitusjon = tilPdfHelseinstitusjon(skadeforklaring.helseinstitusjon)
        val vedleggInfo = tilPdfVedleggInfo(skadeforklaring)
        val dokumentInfo = lagPdfDokumentInfoSkadeforklaring(record.metadata)

        return PdfSkadeforklaring(
            innmelder = innmelder,
            skadelidt = skadelidt,
            tid = tid,
            arbeidetMedIUlykkesoeyeblikket = arbeidsbeskrivelse,
            noeyaktigBeskrivelseAvHendelsen = ulykkesbeskrivelse,
            fravaer = fravaer,
            helseinstitusjon = helseinstitusjon,
            vedleggInfo = vedleggInfo,
            dokumentInfo = dokumentInfo
        )
    }

    private fun tilPdfInnmelder(innmelder: Innmelder?, innmeldersNavn: Navn?, kodeverkHolder: KodeverkHolder): PdfInnmelder {
        return PdfInnmelder(
            norskIdentitetsnummer = Soknadsfelt("Fødselsnummer", innmelder?.norskIdentitetsnummer),
            navn = Soknadsfelt("Navn", innmeldersNavn?.toString().orEmpty()),
            innmelderrolle = Soknadsfelt("Rolle", if (innmelder?.innmelderrolle.isNullOrBlank()) null else kodeverkHolder.mapKodeTilVerdi(innmelder?.innmelderrolle!!, "innmelderrolle"))
        )
    }

    private fun tilPdfSkadelidt(skadelidt: Skadelidt?, skadelidtsNavn: Navn?): PdfSkadelidt {
        return PdfSkadelidt(
            Soknadsfelt("Fødselsnummer", skadelidt?.norskIdentitetsnummer),
            Soknadsfelt("Navn", skadelidtsNavn?.toString().orEmpty())
        )
    }

    private fun tilPdfTid(skadeforklaring: Skadeforklaring): PdfTid {
        return PdfTid(
            tidstype = skadeforklaring.tid.tidstype.uppercase(),
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

    private fun tilPdfFravaer(fravaer: Fravaer, kodeverkHolder: KodeverkHolder): PdfFravaer {
        val foerteTilFravaer = when(fravaer.foerteDinSkadeEllerSykdomTilFravaer) {
            "fravaersdagerUkjent" -> "Ja"
            "treDagerEllerMindre" -> "Ja"
            "merEnnTreDager" -> "Ja"
            "ikkeRelevant" -> "Ikke relevant"
            else -> "Nei"
        }

        return PdfFravaer(
            Soknadsfelt("Førte din skade/sykdom til fravær?", foerteTilFravaer),
            Soknadsfelt("Velg type fravær", if (fravaer.fravaertype.isNullOrBlank()) "" else hentVerdi(fravaer.fravaertype!!, "fravaertype", kodeverkHolder))
        )
    }

    private fun hentVerdi(kode: String, kodeliste: String, kodeverkHolder: KodeverkHolder): String {
        return kodeverkHolder.mapKodeTilVerdi(kode, kodeliste)
    }

    private fun tilPdfHelseinstitusjon(helseinstitusjon: Helseinstitusjon): PdfHelseinstitusjon {
        return PdfHelseinstitusjon(
            erHelsepersonellOppsokt = Soknadsfelt("Ble lege oppsøkt etter skaden?", MapperUtil.jaNei(helseinstitusjon.erHelsepersonellOppsokt)),
            navn = Soknadsfelt("Navn på helseforetak, legevakt eller lege", helseinstitusjon.navn),
            adresse = Soknadsfelt("Adresse", tilPdfAdresse(helseinstitusjon.adresse))
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

    private fun tilPdfVedleggInfo(skadeforklaring: Skadeforklaring): Soknadsfelt<List<String>> {
        val tekster = mutableListOf<String>()
        if (skadeforklaring.vedleggreferanser.isNotEmpty()) {
            tekster.add("Bruker har opplastet vedlegg")
        }
        if (skadeforklaring.skalEttersendeDokumentasjon == "ja") {
            tekster.add("Bruker skal ettersende dokumentasjon")
        }
        else {
            tekster.add("Bruker har ingenting mer å tilføye")
        }

        return Soknadsfelt("Vedlegg", tekster)
    }

    private fun lagPdfDokumentInfoSkadeforklaring(metadata: SkadeforklaringMetadata): PdfDokumentInfoSkadeforklaring {
        return PdfDokumentInfoSkadeforklaring(
            dokumentnavn = "Skadeforklaring ved arbeidsulykke",
            dokumentnummer = "NAV 13-00.21",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = MapperUtil.datoFormatert(metadata.tidspunktMottatt),
            tekster = lagPdfTeksterSkadeforklaring()
        )
    }

    private fun lagPdfTeksterSkadeforklaring(): PdfTeksterSkadeforklaring {
        return PdfTeksterSkadeforklaring(
            innmelderSeksjonstittel = "Om innmelder",
            skadelidtSeksjonstittel = "Den skadelidte",
            tidOgStedSeksjonstittel = "Tid og sted",
            omUlykkenSeksjonstittel = "Om ulykken",
            omSkadenSeksjonstittel = "Om fravær og behandling",
            vedleggSeksjonstittel = "Vedlegg"
        )
    }

}