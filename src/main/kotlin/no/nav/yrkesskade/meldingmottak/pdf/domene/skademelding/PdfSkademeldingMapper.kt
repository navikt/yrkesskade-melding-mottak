package no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.datoFormatert
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.jaNei
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.klokkeslettFormatert
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.skademelding.model.*

object PdfSkademeldingMapper {

    fun tilPdfSkademelding(
        record: SkademeldingInnsendtHendelse,
        kodeverkHolder: KodeverkHolder,
        beriketData: BeriketData? = null
    ) : PdfSkademelding {

        val skademelding = record.skademelding
        val innmelder: PdfInnmelder? = tilPdfInnmelder(skademelding.innmelder, beriketData?.innmeldersNavn)
        val skadelidt: PdfSkadelidt? = tilPdfSkadelidt(skademelding.skadelidt, beriketData?.skadelidtsNavn, beriketData?.skadelidtsBostedsadresse, kodeverkHolder)
        val skade: PdfSkade? = tilPdfSkade(skademelding.skade, kodeverkHolder)
        val hendelsesfakta: PdfHendelsesfakta? = tilPdfHendelsesfakta(skademelding.hendelsesfakta, kodeverkHolder)
        val dokumentInfo: PdfDokumentInfo = lagPdfDokumentInfo(record.metadata)

        return PdfSkademelding(innmelder, skadelidt, skade, hendelsesfakta, dokumentInfo)
    }


    private fun tilPdfInnmelder(innmelder: Innmelder?, innmeldersNavn: Navn?): PdfInnmelder? {
        if (innmelder == null) {
            return null
        }

        return PdfInnmelder(
            norskIdentitetsnummer = Soknadsfelt("Fødselsnummer", innmelder.norskIdentitetsnummer),
            navn = Soknadsfelt("Navn", innmeldersNavn?.toString().orEmpty()),
            paaVegneAv = Soknadsfelt("TODO", innmelder.paaVegneAv),
            innmelderrolle = Soknadsfelt("TODO", innmelder.innmelderrolle),
            altinnrolleIDer = Soknadsfelt("Rolle hentet fra Altinn", innmelder.altinnrolleIDer)
        )
    }

    private fun tilPdfSkadelidt(
        skadelidt: Skadelidt?,
        skadelidtsNavn: Navn?,
        skadelidtsBostedsadresse: no.nav.yrkesskade.meldingmottak.domene.Adresse?,
        kodeverkHolder: KodeverkHolder
    ): PdfSkadelidt? {
        if (skadelidt == null) {
            return null
        }

        return PdfSkadelidt(
            Soknadsfelt("Fødselsnummer", skadelidt.norskIdentitetsnummer),
            Soknadsfelt("Navn", skadelidtsNavn?.toString().orEmpty()),
            Soknadsfelt("Bosted", tilPdfAdresse2(skadelidtsBostedsadresse, kodeverkHolder)),
            tilPdfDekningsforhold(skadelidt.dekningsforhold, kodeverkHolder)
        )
    }

    private fun tilPdfDekningsforhold(dekningsforhold: Dekningsforhold, kodeverkHolder: KodeverkHolder): PdfDekningsforhold {
        return PdfDekningsforhold(
            organisasjonsnummer = Soknadsfelt("Org.nr", dekningsforhold.organisasjonsnummer),
            navnPaaVirksomheten = Soknadsfelt("Bedrift", dekningsforhold.navnPaaVirksomheten),
            virksomhetensAdresse = Soknadsfelt("Virksomhetens adresse", tilPdfAdresse(dekningsforhold.virksomhetensAdresse, kodeverkHolder)),
            stillingstittelTilDenSkadelidte = Soknadsfelt("Stilling", dekningsforhold.stillingstittelTilDenSkadelidte.orEmpty().map { kodeverkHolder.mapKodeTilVerdi(it, "stillingstittel") }),
            rolletype = Soknadsfelt("Rolle", PdfRolletype(dekningsforhold.rolletype, kodeverkHolder.mapKodeTilVerdi(dekningsforhold.rolletype, "rolletype"))),
        )
    }

    private fun tilPdfSkade(skade: Skade, kodeverkHolder: KodeverkHolder): PdfSkade? {
        return PdfSkade(
            alvorlighetsgrad = Soknadsfelt("Hvor alvorlig var hendelsen", if (skade.alvorlighetsgrad == null) "" else kodeverkHolder.mapKodeTilVerdi(skade.alvorlighetsgrad!!, "alvorlighetsgrad")),
            skadedeDeler = tilPdfSkadedeDeler(skade.skadedeDeler, kodeverkHolder),
            antattSykefravaerTabellH = Soknadsfelt("Har den skadelidte hatt fravær", if (skade.antattSykefravaerTabellH == null) "" else kodeverkHolder.mapKodeTilVerdi(skade.antattSykefravaerTabellH!!, "harSkadelidtHattFravaer"))
        )
    }

    private fun tilPdfSkadedeDeler(skadedeDeler: List<SkadetDel>, kodeverkHolder: KodeverkHolder): List<PdfSkadetDel> {
        return skadedeDeler.map { tilPdfSkadetDel(it, kodeverkHolder) }
    }

    private fun tilPdfSkadetDel(skadetDel: SkadetDel, kodeverkHolder: KodeverkHolder): PdfSkadetDel {
        return PdfSkadetDel(
            kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", kodeverkHolder.mapKodeTilVerdi(skadetDel.kroppsdelTabellD, "skadetKroppsdel")),
            skadeartTabellC = Soknadsfelt("Hva slags skade er det", kodeverkHolder.mapKodeTilVerdi(skadetDel.skadeartTabellC, "skadetype"))
        )
    }

    private fun tilPdfHendelsesfakta(hendelsesfakta: Hendelsesfakta, kodeverkHolder: KodeverkHolder): PdfHendelsesfakta {
        return PdfHendelsesfakta(
            tid = PdfTid(
                tidstype = hendelsesfakta.tid.tidstype.value,
                tidspunkt = Soknadsfelt("Når skjedde ulykken som skal meldes?",
                    PdfTidspunkt(
                        dato = datoFormatert(hendelsesfakta.tid.tidspunkt),
                        klokkeslett = klokkeslettFormatert(hendelsesfakta.tid.tidspunkt)
                    )
                ),
                periode = Soknadsfelt("Når skjedde ulykken som skal meldes?",
                    PdfPeriode(
                        fra = datoFormatert(hendelsesfakta.tid.periode?.fra),
                        til = datoFormatert(hendelsesfakta.tid.periode?.til)
                    )
                ),
                ukjent = Soknadsfelt("Når skjedde ulykken som skal meldes?", hendelsesfakta.tid.ukjent)
            ),
            naarSkjeddeUlykken = Soknadsfelt("Innenfor hvilket tidsrom inntraff ulykken?", kodeverkHolder.mapKodeTilVerdi(hendelsesfakta.naarSkjeddeUlykken, "tidsrom")),
            hvorSkjeddeUlykken = Soknadsfelt("Hvor skjedde ulykken", kodeverkHolder.mapKodeTilVerdi(hendelsesfakta.hvorSkjeddeUlykken, "hvorSkjeddeUlykken")),
            ulykkessted = PdfUlykkessted(
                sammeSomVirksomhetensAdresse = Soknadsfelt("Skjedde ulykken på samme adresse", jaNei(hendelsesfakta.ulykkessted.sammeSomVirksomhetensAdresse)),
                adresse = Soknadsfelt("Adresse for ulykken", tilPdfAdresse(hendelsesfakta.ulykkessted.adresse, kodeverkHolder))
            ),
            aarsakUlykkeTabellAogE = Soknadsfelt("Hva var årsaken til hendelsen og bakgrunn for årsaken", hendelsesfakta.aarsakUlykkeTabellAogE.map { kodeverkHolder.mapKodeTilVerdi(it, "aarsakOgBakgrunn") }),
            bakgrunnsaarsakTabellBogG = Soknadsfelt("Hva var bakgrunnen til hendelsen", hendelsesfakta.bakgrunnsaarsakTabellBogG.map { kodeverkHolder.mapKodeTilVerdi(it, "bakgrunnForHendelsen") }),
            stedsbeskrivelseTabellF = Soknadsfelt("Hvilken type arbeidsplass er det", typeArbeidsplass(hendelsesfakta, kodeverkHolder)),
            utfyllendeBeskrivelse = Soknadsfelt("Utfyllende beskrivelse", hendelsesfakta.utfyllendeBeskrivelse)
        )
    }

    private fun typeArbeidsplass(hendelsesfakta: Hendelsesfakta, kodeverkHolder: KodeverkHolder): String? {
        if (hendelsesfakta.stedsbeskrivelseTabellF == null) {
            return null
        }
        return kodeverkHolder.mapKodeTilVerdi(hendelsesfakta.stedsbeskrivelseTabellF!!, "typeArbeidsplass")
    }

    private fun tilPdfAdresse(adresse: Adresse?, kodeverkHolder: KodeverkHolder): PdfAdresse? {
        if (adresse == null) {
            return null
        }

        return PdfAdresse(
            adresselinje1 = adresse.adresselinje1,
            adresselinje2 = adresse.adresselinje2,
            adresselinje3 = adresse.adresselinje3,
            land = landNavnEllerKode(adresse.land, kodeverkHolder)
        )
    }

    private fun tilPdfAdresse2(adresse: no.nav.yrkesskade.meldingmottak.domene.Adresse?, kodeverkHolder: KodeverkHolder): PdfAdresse {
        return PdfAdresse(
            adresselinje1 = adresse?.adresselinje1 ?: "",
            adresselinje2 = adresse?.adresselinje2,
            adresselinje3 = adresse?.adresselinje3,
            land = landNavnEllerKode(adresse?.land, kodeverkHolder)
        )
    }

    private fun landNavnEllerKode(landkode: String?, kodeverkHolder: KodeverkHolder): String? {
        if (landkode == null || landkode == "NO" || landkode == "NOR") return null
        return kodeverkHolder.mapKodeTilVerdi(landkode, "landkoder")
    }

    private fun lagPdfDokumentInfo(metadata: SkademeldingMetadata): PdfDokumentInfo {
        return PdfDokumentInfo(
            dokumentnavn = "Melding om yrkesskade eller yrkessykdom",
            dokumentnummer = "NAV 13",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = datoFormatert(metadata.tidspunktMottatt),
            tekster = lagPdfTekster()
        )
    }

    private fun lagPdfTekster(): PdfTekster {
        return PdfTekster(
            innmelderSeksjonstittel = "Om innmelder",
            tidOgStedSeksjonstittel = "Tid og sted",
            skadelidtSeksjonstittel = "Den skadelidte",
            omUlykkenSeksjonstittel = "Om ulykken",
            omSkadenSeksjonstittel = "Om skaden",
            omSkadenFlereSkader = "Denne skademeldingen inneholder flere skader"
        )
    }

}
