package no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.datoFormatert
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.jaNei
import no.nav.yrkesskade.meldingmottak.pdf.domene.MapperUtil.klokkeslettFormatert
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfAdresse
import no.nav.yrkesskade.meldingmottak.pdf.domene.Soknadsfelt
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.skademelding.model.Adresse
import no.nav.yrkesskade.skademelding.model.Dekningsforhold
import no.nav.yrkesskade.skademelding.model.Hendelsesfakta
import no.nav.yrkesskade.skademelding.model.Innmelder
import no.nav.yrkesskade.skademelding.model.Periode
import no.nav.yrkesskade.skademelding.model.Skade
import no.nav.yrkesskade.skademelding.model.Skadelidt
import no.nav.yrkesskade.skademelding.model.SkadetDel
import no.nav.yrkesskade.skademelding.model.Tidstype
import no.nav.yrkesskade.skademelding.model.Ulykkesadresse
import no.nav.yrkesskade.skademelding.model.Ulykkessted

object PdfSkademeldingMapper {

    fun tilPdfSkademelding(
        record: SkademeldingInnsendtHendelse,
        kodeverkHolder: KodeverkHolder,
        beriketData: BeriketData? = null
    ) : PdfSkademelding {

        val skademelding = record.skademelding
        val erSykdom = skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode

        val innmelder: PdfInnmelder? = tilPdfInnmelder(skademelding.innmelder, beriketData?.innmeldersNavn)
        val skadelidt: PdfSkadelidt? = tilPdfSkadelidt(skademelding.skadelidt, beriketData?.skadelidtsNavn, beriketData?.skadelidtsBostedsadresse, kodeverkHolder)
        val skade: PdfSkade? = tilPdfSkade(skademelding.skade, kodeverkHolder)
        val hendelsesfakta: PdfHendelsesfakta = tilPdfHendelsesfakta(skademelding.hendelsesfakta, erSykdom, kodeverkHolder)
        val dokumentInfo: PdfDokumentInfo = lagPdfDokumentInfo(record.metadata, erSykdom)

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

    private fun tilPdfSkade(skade: Skade, kodeverkHolder: KodeverkHolder): PdfSkade {
        return PdfSkade(
            alvorlighetsgrad = Soknadsfelt("Hvor alvorlig var hendelsen", if (skade.alvorlighetsgrad == null) "" else kodeverkHolder.mapKodeTilVerdi(skade.alvorlighetsgrad!!, "alvorlighetsgrad")),
            skadedeDeler = tilPdfSkadedeDeler(skade.skadedeDeler, kodeverkHolder),
            antattSykefravaer = Soknadsfelt("Har den skadelidte hatt fravær", if (skade.antattSykefravaer == null) null else kodeverkHolder.mapKodeTilVerdi(skade.antattSykefravaer!!, "harSkadelidtHattFravaer"))
        )
    }

    private fun tilPdfSkadedeDeler(skadedeDeler: List<SkadetDel>, kodeverkHolder: KodeverkHolder): List<PdfSkadetDel> {
        return skadedeDeler.map { tilPdfSkadetDel(it, kodeverkHolder) }
    }

    private fun tilPdfSkadetDel(skadetDel: SkadetDel, kodeverkHolder: KodeverkHolder): PdfSkadetDel {
        return PdfSkadetDel(
            kroppsdel = Soknadsfelt("Hvor på kroppen er skaden",
                kodeverkHolder.mapKodeTilVerdi(skadetDel.kroppsdel, "skadetKroppsdel")),
            skadeart = Soknadsfelt("Hva slags skade eller sykdom er det",
                mapSkadetypeEllerSykdomstype(skadetDel.skadeart, kodeverkHolder))
        )
    }

    internal fun mapSkadetypeEllerSykdomstype(skadeart: String, kodeverkHolder: KodeverkHolder): String {
        val skadetype = kodeverkHolder.mapKodeTilVerdi(skadeart, "skadetype")
        if (skadetype == "Ukjent $skadeart") {
            return kodeverkHolder.mapKodeTilVerdi(skadeart, "sykdomstype")
        }
        return skadetype
    }

    private fun tilPdfHendelsesfakta(hendelsesfakta: Hendelsesfakta, erSykdom: Boolean, kodeverkHolder: KodeverkHolder): PdfHendelsesfakta {
        return PdfHendelsesfakta(
            tid = PdfTid(
                tidstype = hendelsesfakta.tid.tidstype.value,
                tidspunkt = Soknadsfelt("Når skjedde ulykken som skal meldes?",
                    PdfTidspunkt(
                        dato = datoFormatert(hendelsesfakta.tid.tidspunkt),
                        klokkeslett = klokkeslettFormatert(hendelsesfakta.tid.tidspunkt)
                    )
                ),
                perioder = Soknadsfelt("Når skjedde ulykken som skal meldes?",
                    hendelsesfakta.tid.perioder?.map { tilPdfPeriode(it) }
                ),
                sykdomPaavist = Soknadsfelt("Når ble sykdommen påvist?",
                    if (hendelsesfakta.tid.sykdomPaavist != null) datoFormatert(hendelsesfakta.tid.sykdomPaavist) else null),
                ukjent = Soknadsfelt("Når skjedde ulykken som skal meldes?", hendelsesfakta.tid.ukjent)
            ),
            naarSkjeddeUlykken = Soknadsfelt("Innenfor hvilket tidsrom inntraff ulykken?",
                kodeverkHolder.mapKodeTilVerdi(hendelsesfakta.naarSkjeddeUlykken, "tidsrom")),
            hvorSkjeddeUlykken = tilHvorSkjeddeUlykken(hendelsesfakta.hvorSkjeddeUlykken, erSykdom, kodeverkHolder),
            ulykkessted = tilPdfUlykkessted(hendelsesfakta.ulykkessted, erSykdom, kodeverkHolder),
            paavirkningsform = Soknadsfelt("Hvilken skadelig påvirkning har personen vært utsatt for",
                hendelsesfakta.paavirkningsform?.map { kodeverkHolder.mapKodeTilVerdi(it, "paavirkningsform") }),
            aarsakUlykke = Soknadsfelt("Hva var årsaken til hendelsen og bakgrunn for årsaken",
                hendelsesfakta.aarsakUlykke?.map { kodeverkHolder.mapKodeTilVerdi(it, "aarsakOgBakgrunn") }),
            bakgrunnsaarsak = Soknadsfelt("Hva var bakgrunnen til hendelsen",
                hendelsesfakta.bakgrunnsaarsak?.map { kodeverkHolder.mapKodeTilVerdi(it, "bakgrunnForHendelsen") }),
            stedsbeskrivelse = Soknadsfelt("Hvilken type arbeidsplass er det", typeArbeidsplass(hendelsesfakta, kodeverkHolder)),
            utfyllendeBeskrivelse = Soknadsfelt("Utfyllende beskrivelse", hendelsesfakta.utfyllendeBeskrivelse)
        )
    }

    private fun tilHvorSkjeddeUlykken(
        hvorSkjeddeUlykken: String?,
        erSykdom: Boolean,
        kodeverkHolder: KodeverkHolder
    ): Soknadsfelt<String> {
        return Soknadsfelt(
            if (erSykdom) "Hvor skjedde hendelsen" else "Hvor skjedde ulykken",
            hvorSkjeddeUlykken?.let { kodeverkHolder.mapKodeTilVerdi(it, "hvorSkjeddeUlykken") }.orEmpty()
        )
    }

    private fun tilPdfUlykkessted(
        ulykkessted: Ulykkessted?,
        erSykdom: Boolean,
        kodeverkHolder: KodeverkHolder
    ): PdfUlykkessted? {
        if (ulykkessted == null) {
            return null
        }
        return PdfUlykkessted(
            sammeSomVirksomhetensAdresse = Soknadsfelt(
                "Skjedde ulykken på samme adresse",
                jaNei(ulykkessted.sammeSomVirksomhetensAdresse)
            ),
            adresse = Soknadsfelt(
                if (erSykdom) "Adresse hvor den skadelige påvirkningen har skjedd" else "Adresse for ulykken",
                tilPdfUlykkesadresse(ulykkessted.adresse, kodeverkHolder)
            )
        )
    }

    private fun tilPdfPeriode(periode: Periode): PdfPeriode =
        PdfPeriode(
            fra = datoFormatert(periode.fra),
            til = datoFormatert(periode.til),
        )

    private fun typeArbeidsplass(hendelsesfakta: Hendelsesfakta, kodeverkHolder: KodeverkHolder): String? {
        if (hendelsesfakta.stedsbeskrivelse == null) {
            return null
        }
        return kodeverkHolder.mapKodeTilVerdi(hendelsesfakta.stedsbeskrivelse!!, "typeArbeidsplass")
    }

    private fun tilPdfAdresse(adresse: Adresse?, kodeverkHolder: KodeverkHolder): PdfAdresse? {
        if (adresse == null) {
            return null
        }

        return PdfAdresse(
            adresselinje1 = adresse.adresselinje1.orEmpty(),
            adresselinje2 = adresse.adresselinje2,
            adresselinje3 = adresse.adresselinje3,
            land = landNavnEllerKode(adresse.land, kodeverkHolder)
        )
    }

    private fun tilPdfUlykkesadresse(adresse: Ulykkesadresse, kodeverkHolder: KodeverkHolder): PdfAdresse {
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

    private fun lagPdfDokumentInfo(metadata: SkademeldingMetadata, erSykdom: Boolean): PdfDokumentInfo {
        return PdfDokumentInfo(
            dokumentnavn = "Melding om yrkesskade eller yrkessykdom",
            dokumentnummer = "NAV 13",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = datoFormatert(metadata.tidspunktMottatt),
            tekster = lagPdfTekster(erSykdom),
            annet = lagPdfAnnet(erSykdom)
        )
    }

    private fun lagPdfTekster(erSykdom: Boolean): PdfTekster {
        return PdfTekster(
            innmelderSeksjonstittel = "Om innmelder",
            tidOgStedSeksjonstittel = "Tid og sted",
            skadelidtSeksjonstittel = "Den skadelidte",
            omUlykkenSeksjonstittel = if (erSykdom) "Om den skadelige påvirkningen" else "Ulykkessted og om ulykken",
            omSkadenSeksjonstittel = "Om skaden",
            omSkadenFlereSkader = "Denne skademeldingen inneholder flere skader"
        )
    }

    private fun lagPdfAnnet(erSykdom: Boolean): PdfAnnet {
        return PdfAnnet(erSykdom)
    }

}
