package no.nav.yrkesskade.skadeforklaring.model

data class Helseinstitusjon(
    val erHelsepersonellOppsokt: String,
    val navn: String?,
    val adresse: Adresse?
)