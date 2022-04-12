package no.nav.yrkesskade.meldingmottak.integration.model

data class Behandler(
    val erBehandlerOppsokt: Boolean,
    val behandlerNavn: String?,
    val adresse: Adresse?
)