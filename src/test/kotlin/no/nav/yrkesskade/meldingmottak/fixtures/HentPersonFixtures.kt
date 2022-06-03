package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.hentperson.*

fun gyldigPersonMedNavnOgVegadresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Ola", null, "Normann", "Ola Normann")),
        emptyList(),
        listOf(bostedVegadresse())
    )
}

fun gyldigPersonMedNavnOgMatrikkeladresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Kari", "Storm", "Hansen", "Kari S Hansen")),
        emptyList(),
        listOf(bostedMatrikkeladresse())
    )
}

fun gyldigPersonMedUkjentBosted(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Espen", null, "Uteligger", "Espen Uteligger")),
        emptyList(),
        listOf(bostedUkjentBosted())
    )
}

fun gyldigPersonMedNavnMenUtenBostedsadresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Kari", "Storm", "Hansen", "Kari S Hansen")),
        emptyList()
    )
}

fun gyldigPersonMedEnkelUtenlandskAdresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Ida", null, "Nilsson", "Ida Nilsson")),
        emptyList(),
        listOf(bostedEnkelUtenlandskAdresse())
    )
}

fun gyldigPersonMedUtenlandskAdresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
        listOf(Navn("Lasse", "Medel", "Svensson", "Lasse Medel Svensson")),
        emptyList(),
        listOf(bostedUtenlandskAdresse())
    )
}

fun gyldigFortroligPersonMedNavnOgVegadresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG)),
        listOf(Navn("Fortrolig", null, "Person", "Fortrolig Person")),
        emptyList(),
        listOf(bostedVegadresse())
    )
}

fun gyldigStrengtFortroligPersonMedNavnOgVegadresse(): Person {
    return Person(
        listOf(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)),
        listOf(Navn("Strengt", "Fortrolig", "Person", "Strengt Fortrolig Person")),
        emptyList(),
        listOf(bostedVegadresse())
    )
}

fun bostedVegadresse(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = Vegadresse(matrikkelId = "12345"),
        matrikkeladresse = null,
        ukjentBosted = null,
        utenlandskAdresse = null
    )
}

fun bostedMatrikkeladresse(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = null,
        matrikkeladresse = Matrikkeladresse(matrikkelId = "6789"),
        ukjentBosted = null,
        utenlandskAdresse = null
    )
}

fun bostedUkjentBosted(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = null,
        matrikkeladresse = null,
        ukjentBosted = UkjentBosted(bostedskommune = "Oslo"),
        utenlandskAdresse = null
    )
}

fun bostedEnkelUtenlandskAdresse(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = null,
        matrikkeladresse = null,
        ukjentBosted = null,
        utenlandskAdresse = UtenlandskAdresse(
            adressenavnNummer = null,
            bygningEtasjeLeilighet = null,
            postboksNummerNavn = "Box 150",
            postkode = "SE-125 24",
            bySted = "Älvsjö",
            regionDistriktOmraade = null,
            landkode = "SWE"
        )
    )
}

fun bostedUtenlandskAdresse(): Bostedsadresse {
    return Bostedsadresse(
        vegadresse = null,
        matrikkeladresse = null,
        ukjentBosted = null,
        utenlandskAdresse = UtenlandskAdresse(
            adressenavnNummer = "Glasfibergatan 10",
            bygningEtasjeLeilighet = "4. etg",
            postboksNummerNavn = "Box 150",
            postkode = "SE-125 24",
            bySted = "Älvsjö",
            regionDistriktOmraade = "Stockholms län",
            landkode = "SWE"
        )
    )
}

