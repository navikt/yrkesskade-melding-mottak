package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringMetadata
import no.nav.yrkesskade.skadeforklaring.v2.model.*

object PdfSkadeforklaringV2Mapper {

    fun tilPdfSkadeforklaring(
        record: SkadeforklaringInnsendingHendelse,
        kodeverkHolder: KodeverkHolder,
        beriketData: BeriketData? = null
    ) : PdfSkadeforklaringV2 {

        val skadeforklaring = record.skadeforklaring
        val innmelder = tilPdfInnmelder(skadeforklaring.innmelder, beriketData?.innmeldersNavn, kodeverkHolder)
        val skadelidt = tilPdfSkadelidt(skadeforklaring.skadelidt, beriketData?.skadelidtsNavn)
        val tid = tilPdfTid(skadeforklaring)
        val arbeidsbeskrivelse = tilPdfArbeidsbeskrivelse(skadeforklaring.arbeidetMedIUlykkesoeyeblikket)
        val ulykkesbeskrivelse = tilPdfUlykkesbeskrivelse(skadeforklaring.noeyaktigBeskrivelseAvHendelsen)
        val fravaer = tilPdfFravaer(skadeforklaring.fravaer, kodeverkHolder)
        val helseinstitusjoner = tilPdfHelseinstitusjoner(skadeforklaring.helseinstitusjoner)
        val vedleggInfo = tilPdfVedleggInfo(skadeforklaring)
        val dokumentInfo = lagPdfDokumentInfoSkadeforklaring(record.metadata)
        val erHelsepersonellOppsokt = tilPdfErHelsepersonellOppsokt(skadeforklaring.erHelsepersonellOppsokt)

        return PdfSkadeforklaringV2(
            innmelder = innmelder,
            skadelidt = skadelidt,
            tid = tid,
            arbeidetMedIUlykkesoeyeblikket = arbeidsbeskrivelse,
            noeyaktigBeskrivelseAvHendelsen = ulykkesbeskrivelse,
            fravaer = fravaer,
            helseinstitusjoner = helseinstitusjoner,
            vedleggInfo = vedleggInfo,
            dokumentInfo = dokumentInfo,
            erHelsepersonellOppsokt = erHelsepersonellOppsokt
        )
    }

    private fun tilPdfInnmelder(innmelder: Innmelder?, innmeldersNavn: Navn?, kodeverkHolder: KodeverkHolder): PdfInnmelderV2 {
        return PdfInnmelderV2(
            norskIdentitetsnummer = Soknadsfelt("Fødselsnummer", innmelder?.norskIdentitetsnummer),
            navn = Soknadsfelt("Navn", innmeldersNavn?.toString().orEmpty()),
            innmelderrolle = Soknadsfelt("Rolle", if (innmelder?.innmelderrolle.isNullOrBlank()) null else kodeverkHolder.mapKodeTilVerdi(innmelder?.innmelderrolle!!, "innmelderrolle"))
        )
    }

    private fun tilPdfSkadelidt(skadelidt: Skadelidt?, skadelidtsNavn: Navn?): PdfSkadelidtV2 {
        return PdfSkadelidtV2(
            Soknadsfelt("Fødselsnummer", skadelidt?.norskIdentitetsnummer),
            Soknadsfelt("Navn", skadelidtsNavn?.toString().orEmpty())
        )
    }

    private fun tilPdfTid(skadeforklaring: Skadeforklaring): PdfTidV2 {
        return PdfTidV2(
            tidstype = skadeforklaring.tid.tidstype.uppercase(),
            tidspunkt = Soknadsfelt(
                "Når skjedde ulykken?",
                PdfTidspunktV2(
                    dato = MapperUtil.datoFormatert(skadeforklaring.tid.tidspunkt),
                    klokkeslett = MapperUtil.klokkeslettFormatert(skadeforklaring.tid.tidspunkt)
                )
            ),
            periode = Soknadsfelt(
                "Når skjedde ulykken?",
                PdfPeriodeV2(
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

    private fun tilPdfErHelsepersonellOppsokt(erHelsepersonellOppsokt: String): Soknadsfelt<String> =
        Soknadsfelt("Ble lege oppsøkt etter skaden?", MapperUtil.jaNei(erHelsepersonellOppsokt))


    private fun tilPdfFravaer(fravaer: Fravaer, kodeverkHolder: KodeverkHolder): PdfFravaerV2 {
        val foerteTilFravaer = when(fravaer.foerteDinSkadeEllerSykdomTilFravaer) {
            "fravaersdagerUkjent" -> "Ja"
            "treDagerEllerMindre" -> "Ja"
            "merEnnTreDager" -> "Ja"
            "ikkeRelevant" -> "Ikke relevant"
            else -> "Nei"
        }

        return PdfFravaerV2(
            Soknadsfelt("Førte din skade/sykdom til fravær?", foerteTilFravaer),
            Soknadsfelt("Velg type fravær", if (fravaer.fravaertype.isNullOrBlank()) "" else hentVerdi(fravaer.fravaertype!!, "fravaertype", kodeverkHolder))
        )
    }

    private fun hentVerdi(kode: String, kodeliste: String, kodeverkHolder: KodeverkHolder): String {
        return kodeverkHolder.mapKodeTilVerdi(kode, kodeliste)
    }

    private fun tilPdfHelseinstitusjoner(helseinstitusjoner: List<Helseinstitusjon>): List<PdfHelseinstitusjonV2> {
        return helseinstitusjoner.map {
            PdfHelseinstitusjonV2(
                navn = Soknadsfelt("Navn på helseforetak, legevakt eller lege", it.navn),
            )
        }
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

    private fun lagPdfDokumentInfoSkadeforklaring(metadata: SkadeforklaringMetadata): PdfDokumentInfoSkadeforklaringV2 {
        return PdfDokumentInfoSkadeforklaringV2(
            dokumentnavn = "Skadeforklaring ved arbeidsulykke",
            dokumentnummer = "NAV 13-00.21",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = MapperUtil.datoFormatert(metadata.tidspunktMottatt),
            tekster = lagPdfTeksterSkadeforklaring()
        )
    }

    private fun lagPdfTeksterSkadeforklaring(): PdfTeksterSkadeforklaringV2 {
        return PdfTeksterSkadeforklaringV2(
            innmelderSeksjonstittel = "Om innmelder",
            skadelidtSeksjonstittel = "Den skadelidte",
            tidOgStedSeksjonstittel = "Tid og sted",
            omUlykkenSeksjonstittel = "Om ulykken",
            omSkadenSeksjonstittel = "Om fravær og behandling",
            vedleggSeksjonstittel = "Vedlegg"
        )
    }

}