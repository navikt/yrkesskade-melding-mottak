package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.hentadresse.KartverketAdresse
import com.expediagroup.graphql.generated.hentadresse.KartverketPostnummeromraade
import com.expediagroup.graphql.generated.hentadresse.KartverketVeg
import com.expediagroup.graphql.generated.hentadresse.KartverketVegadresse

fun gyldigAdresseMedVeg(): KartverketAdresse {
    return KartverketAdresse(
        vegadresse = KartverketVegadresse(
            veg = KartverketVeg("Storgata"),
            nummer = 123,
            bokstav = "B",
            postnummeromraade = KartverketPostnummeromraade("2250", "Plassen"),
            adressetilleggsnavn = null
        ),
        matrikkeladresse = null
    )
}
