package no.nav.yrkesskade.skadeforklaring.v2.model

data class Vedleggreferanse (
    val id: String,
    val navn: String,
    val storrelse: Long,
    val url: String,
)