@file:Suppress("RedundantNullableReturnType")

package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.model.Systemkilde
import no.nav.yrkesskade.skademelding.model.Adresse
import no.nav.yrkesskade.skademelding.model.Dekningsforhold
import no.nav.yrkesskade.skademelding.model.Hendelsesfakta
import no.nav.yrkesskade.skademelding.model.Innmelder
import no.nav.yrkesskade.skademelding.model.Periode
import no.nav.yrkesskade.skademelding.model.Skade
import no.nav.yrkesskade.skademelding.model.Skadelidt
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.SkadetDel
import no.nav.yrkesskade.skademelding.model.Tid
import no.nav.yrkesskade.skademelding.model.Tidstype
import no.nav.yrkesskade.skademelding.model.Ulykkesadresse
import no.nav.yrkesskade.skademelding.model.Ulykkessted
import java.time.LocalDate
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

fun skademeldingInnsendtHendelseForSykdom(): SkademeldingInnsendtHendelse {
    return SkademeldingInnsendtHendelse(
        metadata = metadata(),
        skademelding = skademeldingSykdom(),
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

private fun skademeldingSykdom(): Skademelding {
    return Skademelding(
        innmelder = innmelder(),
        skadelidt = skadelidt(),
        skade = skadeSykdom(),
        hendelsesfakta = hendelsesfaktaSykdom()
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
        antattSykefravaer = "merEnnTreDager"
    )
}

private fun skadeSykdom(): Skade {
    return Skade(
        alvorlighetsgrad = "livstruendeSykdomEllerSkade",
        skadedeDeler = listOf(
            SkadetDel("ondartetSvulst", "ansikt"),
            SkadetDel("bruddskade", "venstreArmOgAlbue")
        ),
        antattSykefravaer = null
    )
}

private fun hendelsesfakta(): Hendelsesfakta {
    return Hendelsesfakta(
        tid = Tid(
            tidstype = Tidstype.tidspunkt,
            tidspunkt = OffsetDateTime.of(2022, Month.FEBRUARY.value, 28, 16, 15, 0, 0, ZoneOffset.UTC),
            perioder = null,
            ukjent = false
        ),
        naarSkjeddeUlykken = "iAvtaltArbeidstid",
        hvorSkjeddeUlykken = "arbeidsstedUte",
        ulykkessted = Ulykkessted(
            sammeSomVirksomhetensAdresse = true,
            adresse = Ulykkesadresse(
                adresselinje1 = "Storgaten 13",
                adresselinje2 = "2345 Småbygda",
                adresselinje3 = null,
                land = "SWE"
            )
        ),
        aarsakUlykke = listOf(
            "fallAvPerson",
            "velt"
        ),
        bakgrunnsaarsak = listOf(
            "defektUtstyr",
            "feilPlassering",
            "mangelfullOpplaering"
        ),
        stedsbeskrivelse = "industriellVirksomhet",
        utfyllendeBeskrivelse = "Dette er en veldig lang utfyllende beskrivelse bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla bla blabla"
    )
}

private fun hendelsesfaktaSykdom(): Hendelsesfakta {
    return Hendelsesfakta(
        tid = Tid(
            tidstype = Tidstype.periode,
            tidspunkt = null,
            perioder = listOf(
                Periode(
                    fra = LocalDate.of(2021, Month.OCTOBER, 1),
                    til = LocalDate.of(2022, Month.JANUARY, 10)
                ),
                Periode(
                    fra = LocalDate.of(2022, Month.MARCH, 13),
                    til = LocalDate.of(2022, Month.MAY, 31)
                )
            ),
            sykdomPaavist = LocalDate.of(2021, Month.OCTOBER, 20),
            ukjent = false
        ),
        naarSkjeddeUlykken = "iAvtaltArbeidstid",
        hvorSkjeddeUlykken = "arbeidsstedUte",
        ulykkessted = Ulykkessted(
            sammeSomVirksomhetensAdresse = true,
            adresse = Ulykkesadresse(
                adresselinje1 = "Storgaten 13",
                adresselinje2 = "2345 Småbygda",
                adresselinje3 = null,
                land = "SWE"
            )
        ),
        paavirkningsform = listOf(
            "stoevpaavirkning",
            "kjemikalierEllerLoesemidler"
        ),
        aarsakUlykke = null,
        bakgrunnsaarsak = null,
        stedsbeskrivelse = null,
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
            adresselinje2 = "7730 Småby",
            adresselinje3 = null,
            land = "NOR"
        )
    )
}
