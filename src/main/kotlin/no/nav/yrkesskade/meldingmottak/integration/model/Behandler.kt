package no.nav.yrkesskade.meldingmottak.integration.model

data class Behandler(
    val erBehandlerOppsokt: String,
    val behandlerNavn: String?,
    val adresse: Adresse?
)