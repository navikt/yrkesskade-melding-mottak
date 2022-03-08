package no.nav.yrkesskade.meldingmottak.domene

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)

data class Adresse(
    val adresselinje1: String,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String
)

data class BeriketData(
    val innmeldersNavn: Navn?,
    val skadelidtsNavn: Navn?,
    val skadelidtsBostedsadresse: Adresse?,
)

