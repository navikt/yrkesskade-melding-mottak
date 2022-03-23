package no.nav.yrkesskade.meldingmottak.pdf

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Land
import no.nav.yrkesskade.meldingmottak.domene.Landkode
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.pdf.domene.*
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.skademelding.model.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PdfSkademeldingMapper {

    fun tilPdfSkademelding(
        record: SkademeldingInnsendtHendelse,
        alleLand: Map<Landkode, Land>,
        beriketData: BeriketData? = null
    ) : PdfSkademelding {

        val skademelding = record.skademelding
        val innmelder: PdfInnmelder? = tilPdfInnmelder(skademelding.innmelder, beriketData?.innmeldersNavn)
        val skadelidt: PdfSkadelidt? = tilPdfSkadelidt(skademelding.skadelidt, beriketData?.skadelidtsNavn, beriketData?.skadelidtsBostedsadresse, alleLand)
        val skade: PdfSkade? = tilPdfSkade(skademelding.skade)
        val hendelsesfakta: PdfHendelsesfakta? = tilPdfHendelsesfakta(skademelding.hendelsesfakta, alleLand)
        val dokumentInfo: PdfDokumentInfo = lagPdfDokumentInfo(record.metadata)

        return PdfSkademelding(innmelder, skadelidt, skade, hendelsesfakta, dokumentInfo)
    }


    private fun tilPdfInnmelder(innmelder: Innmelder?, innmeldersNavn: Navn?): PdfInnmelder? {
        if (innmelder == null) {
            return null
        }

        return PdfInnmelder(
            norskIdentitetsnummer = Soknadsfelt("Fødselsnummer", innmelder.norskIdentitetsnummer),
            navn = Soknadsfelt("Navn", tilString(innmeldersNavn)),
            paaVegneAv = Soknadsfelt("TODO", innmelder.paaVegneAv),
            innmelderrolle = Soknadsfelt("TODO", innmelder.innmelderrolle.value),
            altinnrolleIDer = Soknadsfelt("Rolle hentet fra Altinn", innmelder.altinnrolleIDer)
        )
    }

    private fun tilPdfSkadelidt(
        skadelidt: Skadelidt?,
        skadelidtsNavn: Navn?,
        skadelidtsBostedsadresse: no.nav.yrkesskade.meldingmottak.domene.Adresse?,
        alleLand: Map<Landkode, Land>
    ): PdfSkadelidt? {
        if (skadelidt == null) {
            return null
        }

        return PdfSkadelidt(
            Soknadsfelt("Fødselsnummer", skadelidt.norskIdentitetsnummer),
            Soknadsfelt("Navn", tilString(skadelidtsNavn)),
            Soknadsfelt("Bosted", tilPdfAdresse2(skadelidtsBostedsadresse, alleLand)),
            tilPdfDekningsforhold(skadelidt.dekningsforhold)
        )
    }

    private fun tilPdfDekningsforhold(dekningsforhold: Dekningsforhold): PdfDekningsforhold {
        return PdfDekningsforhold(
            organisasjonsnummer = Soknadsfelt("Org.nr", dekningsforhold.organisasjonsnummer),
            navnPaaVirksomheten = Soknadsfelt("Bedrift", dekningsforhold.navnPaaVirksomheten),
            stillingstittelTilDenSkadelidte = Soknadsfelt("Stilling", dekningsforhold.stillingstittelTilDenSkadelidte.map { it.value }),
            rolletype = Soknadsfelt("Rolle", dekningsforhold.rolletype.value),
        )
    }

    private fun tilPdfSkade(skade: Skade?): PdfSkade? {
        if (skade == null) {
            return null
        }

        return PdfSkade(
            alvorlighetsgrad = Soknadsfelt("Hvor alvorlig var hendelsen", skade.alvorlighetsgrad?.value),
            skadedeDeler = tilPdfSkadedeDeler(skade.skadedeDeler),
            antattSykefravaerTabellH = Soknadsfelt("Har den skadelidte hatt fravær", skade.antattSykefravaerTabellH.value)
        )
    }

    private fun tilPdfSkadedeDeler(skadedeDeler: List<SkadetDel>): List<PdfSkadetDel> {
        return skadedeDeler.map { tilPdfSkadetDel(it) }
    }

    private fun tilPdfSkadetDel(skadetDel: SkadetDel): PdfSkadetDel {
        return PdfSkadetDel(
            kroppsdelTabellD = Soknadsfelt("Hvor på kroppen er skaden", skadetDel.kroppsdelTabellD.value),
            skadeartTabellC = Soknadsfelt("Hva slags skade er det", skadetDel.skadeartTabellC.value)
        )
    }

    private fun tilPdfHendelsesfakta(hendelsesfakta: Hendelsesfakta?, alleLand: Map<Landkode, Land>): PdfHendelsesfakta? {
        if (hendelsesfakta == null) {
            return null
        }

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
            naarSkjeddeUlykken = Soknadsfelt("Innenfor hvilket tidsrom inntraff ulykken?", hendelsesfakta.naarSkjeddeUlykken.value),
            hvorSkjeddeUlykken = Soknadsfelt("Hvor skjedde ulykken", hendelsesfakta.hvorSkjeddeUlykken.value),
            ulykkessted = PdfUlykkessted(
                sammeSomVirksomhetensAdresse = Soknadsfelt("Skjedde ulykken på samme adresse", jaNei(hendelsesfakta.ulykkessted.sammeSomVirksomhetensAdresse)),
                adresse = Soknadsfelt("Adresse", tilPdfAdresse(hendelsesfakta.ulykkessted.adresse, alleLand))
            ),
            aarsakUlykkeTabellAogE = Soknadsfelt("Hva var årsaken til hendelsen og bakgrunn for årsaken", hendelsesfakta.aarsakUlykkeTabellAogE.map { it.value }),
            bakgrunnsaarsakTabellBogG = Soknadsfelt("Hva var bakgrunnen til hendelsen", hendelsesfakta.bakgrunnsaarsakTabellBogG.map { it.value }),
            stedsbeskrivelseTabellF = Soknadsfelt("Hvilken type arbeidsplass er det", hendelsesfakta.stedsbeskrivelseTabellF.value),
            utfyllendeBeskrivelse = Soknadsfelt("Utfyllende beskrivelse", hendelsesfakta.utfyllendeBeskrivelse)
        )
    }

    private fun tilPdfAdresse(adresse: Adresse?, alleLand: Map<Landkode, Land>): PdfAdresse? {
        if (adresse == null) {
            return null
        }

        return PdfAdresse(
            adresselinje1 = adresse.adresselinje1,
            adresselinje2 = adresse.adresselinje2,
            adresselinje3 = adresse.adresselinje3,
            land = landNavnEllerKode(adresse.land, alleLand)
        )
    }

    private fun tilPdfAdresse2(adresse: no.nav.yrkesskade.meldingmottak.domene.Adresse?, alleLand: Map<Landkode, Land>): PdfAdresse {
        return PdfAdresse(
            adresselinje1 = adresse?.adresselinje1 ?: "",
            adresselinje2 = adresse?.adresselinje2,
            adresselinje3 = adresse?.adresselinje3,
            land = landNavnEllerKode(adresse?.land, alleLand)
        )
    }

    private fun landNavnEllerKode(landkode: String?, alleLand: Map<Landkode, Land>): String? {
        if (landkode == null || landkode == "NO" || landkode == "NOR") return null
        val land = alleLand[landkode]
        return land?.navn ?: landkode
    }

    private fun lagPdfDokumentInfo(metadata: SkademeldingMetadata): PdfDokumentInfo {
        return PdfDokumentInfo(
            dokumentnavn = "Melding om yrkesskade eller yrkessykdom",
            dokumentnummer = "NAV 13",
            dokumentDatoPrefix = "Innsendt digitalt ",
            dokumentDato = datoFormatert(LocalDate.ofInstant(metadata.tidspunktMottatt, ZoneId.of("Europe/Oslo"))),
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

    private fun datoFormatert(dateTime: OffsetDateTime?): String {
        return dateTime?.toLocalDate()?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""
    }

    private fun datoFormatert(date: LocalDate?): String {
        return date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""
    }

    private fun klokkeslettFormatert(dateTime: OffsetDateTime?): String {
        return dateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("hh.mm")) ?: ""
    }

    private fun jaNei(boolean: Boolean): String {
        return when (boolean) {
            true -> "Ja"
            false -> "Nei"
        }
    }

    private fun tilString(navn: Navn?): String {
        if (navn == null) {
            return ""
        }

        return buildString {
            append("${navn.fornavn} ")
            if (navn.mellomnavn != null) append("${navn.mellomnavn} ")
            append(navn.etternavn)
        }
    }

}
