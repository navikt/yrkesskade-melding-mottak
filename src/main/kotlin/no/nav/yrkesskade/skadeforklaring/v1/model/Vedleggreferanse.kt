package no.nav.yrkesskade.skadeforklaring.v1.model

data class Vedleggreferanse (
    val id: String,
    val navn: String,
    val storrelse: Long,
    val url: String,
)