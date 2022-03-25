package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode

fun noenLand(): Map<KodeverkKode, KodeverkVerdi> {
    return mapOf(
        "NO" to KodeverkVerdi("NO", "nb", "NORGE", null),
        "SE" to KodeverkVerdi("SE", "nb","SVERIGE", null)
    )
}