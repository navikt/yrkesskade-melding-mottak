package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi

fun noenLand(): Map<KodeverkKode, KodeverkVerdi> {
    return mapOf(
        "NO" to KodeverkVerdi("NO", "NORGE"),
        "SE" to KodeverkVerdi("SE", "SVERIGE")
    )
}