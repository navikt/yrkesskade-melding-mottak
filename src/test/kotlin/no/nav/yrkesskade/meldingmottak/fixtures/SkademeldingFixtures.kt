@file:Suppress("RedundantNullableReturnType")

package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.model.*
import no.nav.yrkesskade.skademelding.model.*
import java.time.LocalDateTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun enkelSkademeldingInnsendtHendelse(): SkademeldingInnsendtHendelse {
    return SkademeldingInnsendtHendelse(
        metadata = metadata(),
        skademelding = enkelSkademelding(),
        beriketData = SkademeldingBeriketData(
            innmeldersOrganisasjonsnavn = "NAV IT" to Systemkilde.ENHETSREGISTERET
        )
    )
}

private fun enkelSkademelding(): Skademelding {
    return Skademelding(
        innmelder = innmelder(),
        skadelidt = skadelidt(),
        skade = skade(),
        hendelsesfakta = hendelsesfakta()
    )
}

private fun innmelder(): Innmelder {
    return Innmelder(
        norskIdentitetsnummer = "12345677777",
        paaVegneAv = "123454321",
        innmelderrolle = "virksomhetsrepresentant",
        altinnrolleIDer = listOf("111", "22")
    )
}

private fun skadelidt(): Skadelidt {
    return Skadelidt(
        norskIdentitetsnummer = "11111177777",
        dekningsforhold = Dekningsforhold(
            organisasjonsnummer = "123456789",
            navnPaaVirksomheten = "Bedriften AS",
            virksomhetensAdresse = Adresse(
                adresselinje1 = "Virksomhetsgata 70",
                adresselinje2 = "9955 Industribyen",
                adresselinje3 = null,
                land = "SWE"
            ),
            stillingstittelTilDenSkadelidte = listOf(
                "agroteknikere",
                "altmuligmann"
            ),
            rolletype = "arbeidstaker"
        )
    )
}

private fun skade(): Skade {
    return Skade(
        alvorlighetsgrad = "livstruendeSykdomEllerSkade",
        skadedeDeler = listOf(
            SkadetDel("etsing", "ansikt"),
            SkadetDel("bruddskade", "venstreArmOgAlbue")
        ),
        antattSykefravaerTabellH = "merEnnTreDager"
    )
}

private fun hendelsesfakta(): Hendelsesfakta {
    return Hendelsesfakta(
        tid = Tid(
            tidstype = Tidstype.tidspunkt,
            tidspunkt = OffsetDateTime.of(2022, Month.FEBRUARY.value, 28, 16, 15, 0, 0, ZoneOffset.UTC),
            periode = null,
            ukjent = false
        ),
        naarSkjeddeUlykken = "iAvtaltArbeidstid",
        hvorSkjeddeUlykken = "arbeidsstedUte",
        ulykkessted = Ulykkessted(
            sammeSomVirksomhetensAdresse = true,
            adresse = Adresse(
                adresselinje1 = "Storgaten 13",
                adresselinje2 = "2345 Sm??bygda",
                adresselinje3 = null,
                land = "SWE"
            )
        ),
        aarsakUlykkeTabellAogE = listOf(
            "fallAvPerson",
            "velt"
        ),
        bakgrunnsaarsakTabellBogG = listOf(
            "defektUtstyr",
            "feilPlassering",
            "mangelfullOpplaering"
        ),
        stedsbeskrivelseTabellF = "industriellVirksomhet",
        utfyllendeBeskrivelse = "Dette er en veldig lang utfyllende beskrivelse bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla"
    )
}

private fun metadata(): SkademeldingMetadata {
    return SkademeldingMetadata(
        kilde = "web",
        tidspunktMottatt = LocalDateTime.of(2022, Month.FEBRUARY, 28, 17, 30, 0).toInstant(ZoneOffset.ofHours(1)),
        spraak = Spraak.NB,
        navCallId = "ABC-123456789"
    )
}

fun beriketData(): BeriketData {
    return BeriketData(
        innmeldersNavn = Navn("Inn", null, "Melder"),
        skadelidtsNavn = Navn("Ska", "De", "Lidt"),
        skadelidtsBostedsadresse = no.nav.yrkesskade.meldingmottak.domene.Adresse(
            adresselinje1 = "Stigen 7A",
            adresselinje2 = "7730 Sm??by",
            adresselinje3 = null,
            land = "NOR"
        )
    )
}
