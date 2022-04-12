package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi

fun noenLand(): Map<KodeverkKode, KodeverkVerdi> =
    mapOf(
        "NO" to KodeverkVerdi("NO", "NORGE"),
        "SE" to KodeverkVerdi("SE", "SVERIGE")
    )


fun fravaertyper(): Map<KodeverkKode, KodeverkVerdi> =
    mapOf(
        Pair("sykemelding", KodeverkVerdi("sykemelding", "Sykemelding")),
        Pair("egenmelding", KodeverkVerdi("egenmelding", "Egenmelding")),
        Pair("kombinasjonSykemeldingEgenmelding", KodeverkVerdi("kombinasjonSykemeldingEgenmelding", "En kombinasjon av egenmelding og sykemelding")),
        Pair("alternativenePasserIkke", KodeverkVerdi("alternativenePasserIkke", "Alternativene passer ikke"))
    )
