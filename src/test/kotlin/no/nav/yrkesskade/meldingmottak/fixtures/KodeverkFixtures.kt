package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto

fun noenLand(): Map<String, KodeverdiDto> =
    mapOf(
        "NOR" to KodeverdiDto("NOR", "NORGE"),
        "SWE" to KodeverdiDto("SWE", "SVERIGE")
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

fun innmelderroller(): Map<String, KodeverdiDto> =
    mapOf(
        "denSkadelidte" to KodeverdiDto("denSkadelidte", "Den skadelidte selv"),
        "vergeOgForesatt" to KodeverdiDto("vergeOgForesatt", "Verge/Foresatt")
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
        "arbeidsstedUte" to KodeverdiDto("arbeidsstedUte", "På arbeidsstedet ute")
    )

fun aarsakBakgrunn(): Map<String, KodeverdiDto> =
    mapOf(
        "fallAvPerson" to KodeverdiDto("fallAvPerson", "Fall av person"),
        "velt" to KodeverdiDto("velt", "Velt")
    )

fun bakgrunnForHendelsen(): Map<String, KodeverdiDto> =
    mapOf(
        "defektUtstyr" to KodeverdiDto("defektUtstyr", "Defekt utstyr"),
        "feilPlassering" to KodeverdiDto("feilPlassering", "Feil plassering"),
        "mangelfullOpplaering" to KodeverdiDto("mangelfullOpplaering", "Mangelfull opplæring")
    )

fun typeArbeidsplass(): Map<String, KodeverdiDto> =
    mapOf(
        "industriellVirksomhet" to KodeverdiDto("industriellVirksomhet", "Plass for industriell virksomhet")
    )

fun skadetyper(): Map<String, KodeverdiDto> =
    mapOf(
        "etsing" to KodeverdiDto("etsing", "Etsing"),
        "bruddskade" to KodeverdiDto("bruddskade", "Bruddskade")
    )

fun skadetKroppsdel(): Map<String, KodeverdiDto> =
    mapOf(
        "ansikt" to KodeverdiDto("ansikt", "Ansikt"),
        "venstreArmOgAlbue" to KodeverdiDto("venstreArmOgAlbue", "Arm/albue, venstre")
    )

fun harSkadelidtHattFravaer(): Map<String, KodeverdiDto> =
    mapOf(
        "merEnnTreDager" to KodeverdiDto("merEnnTreDager", "Kjent fravær mer enn 3 dager")
    )

fun alvorlighetsgrad(): Map<String, KodeverdiDto> =
    mapOf(
        "livstruendeSykdomEllerSkade" to KodeverdiDto("livstruendeSykdomEllerSkade", "Livstruende sykdom/skade")
    )

fun paavirkningsform(): Map<String, KodeverdiDto> =
    mapOf(
        "kjemikalierEllerLoesemidler" to KodeverdiDto("kjemikalierEllerLoesemidler", "Kjemikalier, løsemidler, gift, gass, væske o.l."),
        "stoevpaavirkning" to KodeverdiDto("stoevpaavirkningstoevpaavirkning", "Støvpåvirkning, stenstøv, asbest o.l.")
    )
