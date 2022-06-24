package no.nav.yrkesskade.meldingmottak.pdf.domene


/**
 * Abstrakt klasse som PDF-dataklasser kan arve fra, som f.eks. skademelding og skadeforklaring.
 */
abstract class PdfData



data class Soknadsfelt<T>(
    val label: String,
    val verdi: T
)

data class PdfAdresse(
    val adresselinje1: String,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String?
)
