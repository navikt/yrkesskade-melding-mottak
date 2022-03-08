package no.nav.yrkesskade.meldingmottak.clients.graphql

import com.expediagroup.graphql.generated.HentAdresse
import com.expediagroup.graphql.generated.HentPerson
import com.expediagroup.graphql.generated.hentperson.UkjentBosted
import com.expediagroup.graphql.generated.hentperson.UtenlandskAdresse
import no.nav.yrkesskade.meldingmottak.domene.Adresse
import no.nav.yrkesskade.meldingmottak.domene.Navn

fun tilNavn(personResult: HentPerson.Result): Navn? {
    val navn = personResult.hentPerson?.navn?.get(0)
    return Navn(navn?.fornavn ?: "", navn?.mellomnavn, navn?.etternavn ?: "")
}

fun extractMatrikkelId(personResult: HentPerson.Result): com.expediagroup.graphql.generated.Long? {
    val bostedsadresse = personResult.hentPerson?.bostedsadresse?.get(0)
    val matrikkelIdVegadresse = bostedsadresse?.vegadresse?.matrikkelId
    val matrikkelIdMatrikkeladresse = bostedsadresse?.matrikkeladresse?.matrikkelId

    return matrikkelIdVegadresse ?: matrikkelIdMatrikkeladresse
}

fun tilAdresse(result: HentAdresse.Result): Adresse? {
    val vegadresse = result.hentAdresse?.vegadresse
    if (vegadresse != null) {
        val linje1 = buildString {
            if (vegadresse.veg?.adressenavn != null) append("${vegadresse.veg?.adressenavn} ")
            if (vegadresse.nummer != null) append("${vegadresse.nummer}")
            if (vegadresse.bokstav != null) append("${vegadresse.bokstav}")
        }
        val linje2 = buildString {
            if (vegadresse.postnummeromraade?.postnummer != null) append("${vegadresse.postnummeromraade?.postnummer} ")
            if (vegadresse.postnummeromraade?.poststed != null) append("${vegadresse.postnummeromraade?.poststed}")
        }
        val linje3 = vegadresse.adressetilleggsnavn
        return Adresse(linje1, linje2, linje3, "")
    }

    val matrikkeladresse = result.hentAdresse?.matrikkeladresse
    if (matrikkeladresse != null) {
        val linje1 = matrikkeladresse.adressetilleggsnavn ?: ""
        val linje2 = buildString {
            if (matrikkeladresse.postnummeromraade?.postnummer != null) append("${matrikkeladresse.postnummeromraade?.postnummer} ")
            if (matrikkeladresse.postnummeromraade?.poststed != null) append("${matrikkeladresse.postnummeromraade?.poststed}")
        }
        return Adresse(linje1, linje2, "", "")
    }

    return null
}


fun tilAdresse(ukjentBosted: UkjentBosted): Adresse? {
    val linje1 = ukjentBosted.bostedskommune ?: ""
    return Adresse(linje1, "", "", "")
}


fun tilAdresse(utlAdr: UtenlandskAdresse): Adresse? {
    val linje1 = buildString {
        if (utlAdr.adressenavnNummer != null) append("${utlAdr.adressenavnNummer} ")
        if (utlAdr.bygningEtasjeLeilighet != null) append("${utlAdr.bygningEtasjeLeilighet} ")
        if (utlAdr.postboksNummerNavn != null) append("${utlAdr.postboksNummerNavn}")
    }
    val linje2 = buildString {
        if (utlAdr.postkode != null) append("${utlAdr.postkode} ")
        if (utlAdr.bySted != null) append("${utlAdr.bySted}")
    }
    val linje3 = utlAdr.regionDistriktOmraade
    val land = utlAdr.landkode
    return Adresse(linje1, linje2, linje3, land)
}






