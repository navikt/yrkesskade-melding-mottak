package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.Land
import no.nav.yrkesskade.meldingmottak.domene.Landkode

fun noenLand(): Map<Landkode, Land> {
    return mapOf(
        "NO" to Land("NO", "NORGE"),
        "SE" to Land("SE", "SVERIGE"),
    )
}