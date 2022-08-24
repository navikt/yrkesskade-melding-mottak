package no.nav.yrkesskade.meldingmottak.util.ruting

data class Rutingfil(
    val rutingregler: Rutingregler
)

data class Rutingregler(
    val yrkessykdom: Yrkessykdomsregler
)

data class Yrkessykdomsregler(
    val regel1: Kodeverdiregel,
    val regel2: Kodeverdiregel
)

data class Kodeverdiregel(
    val kodeverdier: List<String>,
    val enhet: String?
)