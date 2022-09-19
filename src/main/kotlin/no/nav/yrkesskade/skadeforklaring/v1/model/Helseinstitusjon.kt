package no.nav.yrkesskade.skadeforklaring.v1.model

data class Helseinstitusjon(
    val erHelsepersonellOppsokt: String,
    val navn: String?,
    val adresse: Adresse?
)