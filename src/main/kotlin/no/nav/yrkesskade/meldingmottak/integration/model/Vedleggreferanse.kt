package no.nav.yrkesskade.meldingmottak.integration.model

data class Vedleggreferanse (
    val id: String,
    val navn: String,
    val storrelse: Long,
    val url: String,
)