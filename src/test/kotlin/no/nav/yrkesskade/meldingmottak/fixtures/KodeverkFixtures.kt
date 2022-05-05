package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi

fun noenLand(): Map<String, KodeverdiDto> =
    mapOf(
        "NO" to KodeverdiDto("NO", "NORGE"),
        "SE" to KodeverdiDto("SE", "SVERIGE")
    )


fun fravaertyper(): Map<String, KodeverdiDto> =
    mapOf(
        Pair("sykemelding", KodeverdiDto("sykemelding", "Sykemelding")),
        Pair("egenmelding", KodeverdiDto("egenmelding", "Egenmelding")),
        Pair("kombinasjonSykemeldingEgenmelding", KodeverdiDto("kombinasjonSykemeldingEgenmelding", "En kombinasjon av egenmelding og sykemelding")),
        Pair("alternativenePasserIkke", KodeverdiDto("alternativenePasserIkke", "Alternativene passer ikke"))
    )

fun rolletyper(): Map<String, KodeverdiDto> =
    mapOf(
        "arbeidstaker" to KodeverdiDto("arbeidstaker", "Arbeidstaker")
    )

fun stillingstitler(): Map<String, KodeverdiDto> =
    mapOf(
        "altmuligmann" to KodeverdiDto("altmuligmann","Altmuligmann"),
        "agroteknikere" to KodeverdiDto("agroteknikere", "Agroteknikere")
    )

fun tidsrom(): Map<String, KodeverdiDto> =
    mapOf(
        "iAvtaltArbeidstid" to KodeverdiDto("iAvtaltArbeidstid", "I avtalt arbeidstid")
    )

fun hvorSkjeddeUlykken(): Map<String, KodeverdiDto> =
    mapOf(
        "pArbeidsstedetUte" to KodeverdiDto("pArbeidsstedetUte", "På arbeidstedet ute")
    )

fun aarsakBakgrunn(): Map<String, KodeverdiDto> =
    mapOf(
        "fallAvPerson" to KodeverdiDto("fallAvPerson", "Fall av person"),
        "kjemikalier" to KodeverdiDto("kjemikalier", "Kjemikalier")
    )

fun bakgrunnForHendelsen(): Map<String, KodeverdiDto> =
    mapOf(
        "defektUtstyr" to KodeverdiDto("defektUtstyr", "Defekt utstyr"),
        "feilPlassering" to KodeverdiDto("feilPlassering", "Feil plasseringen"),
        "mangelfullOpplRing" to KodeverdiDto("mangelfullOpplRing", "Mangelfull opplæring")
    )

fun typeArbeidsplass(): Map<String, KodeverdiDto> =
    mapOf(
        "plassForIndustriellVirksomhet" to KodeverdiDto("plassForIndustriellVirksomhet", "Plass for industriell virksomhet")
    )

fun skadetyper(): Map<String, KodeverdiDto> =
    mapOf(
        "etsing" to KodeverdiDto("etsing", "Etsing"),
        "knokkelbrudd" to KodeverdiDto("knokkelbrudd", "Knokkelbrudd")
    )

fun skadetKroppsdel(): Map<String, KodeverdiDto> =
    mapOf(
        "ansikt" to KodeverdiDto("ansikt", "Ansikt"),
        "armSlashAlbueCommaVenstre" to KodeverdiDto("armSlashAlbueCommanVenstre", "Arm/albue, venstre")
    )

fun harSkadelidtHattFravaer(): Map<String, KodeverdiDto> =
    mapOf(
        "kjentFravRMerEnn3Dager" to KodeverdiDto("kjentFravRMerEnn3Dager", "Kjent fravær mer enn 3 dager")
    )

fun alvorlighetsgrad(): Map<String, KodeverdiDto> =
    mapOf(
        "andreLivstruendeSykdomSlashSkade" to KodeverdiDto("andreLivstruendeSykdomSlashSkade", "Andre livstruende sykdom/skade")
    )
