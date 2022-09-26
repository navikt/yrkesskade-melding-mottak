package no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling

data class ArbeidsfordelingResponse(val enheter: List<EnhetResponse>)

data class EnhetResponse(
    val enhetNr: String,
    val navn: String
)
