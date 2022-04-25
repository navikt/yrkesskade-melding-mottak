package no.nav.yrkesskade.meldingmottak.integration.model

data class Helseinstitusjon(
    val erHelsepersonellOppsokt: String,
    val navn: String?,
    val adresse: Adresse?
)