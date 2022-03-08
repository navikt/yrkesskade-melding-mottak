package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.hentadresse.*

fun gyldigVegadresse(): KartverketAdresse {
    return KartverketAdresse(
        vegadresse = KartverketVegadresse(
            veg = KartverketVeg("Storgata"),
            nummer = 123,
            bokstav = "B",
            postnummeromraade = KartverketPostnummeromraade("2250", "Plassen"),
            adressetilleggsnavn = "Tillegg"
        ),
        matrikkeladresse = null
    )
}

fun gyldigMatrikkeladresse(): KartverketAdresse {
    return KartverketAdresse(
        null,
        matrikkeladresse = KartverketMatrikkeladresse(
            adressetilleggsnavn = "Tilleggsnavn",
            postnummeromraade = KartverketPostnummeromraade("5700", "Kollen")
        )
    )
}

