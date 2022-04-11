package no.nav.yrkesskade.meldingmottak.pdf.domene


/**
 * Abstrakt klasse som PDF-dataklasser kan arve fra, som f.eks. skademelding og skadeforklaring.
 */
abstract class PdfData



data class Soknadsfelt<T>(
    val label: String,
    val verdi: T
)

data class PdfTid(
    val tidstype: String,
    val tidspunkt: Soknadsfelt<PdfTidspunkt>,
    val periode: Soknadsfelt<PdfPeriode>,
    val ukjent: Soknadsfelt<Boolean?>
)

data class PdfTidspunkt(
    val dato: String,
    val klokkeslett: String
)

data class PdfPeriode(
    val fra: String,
    val til: String
)

data class PdfAdresse(
    val adresselinje1: String,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String?
)

data class PdfDokumentInfo(
    val dokumentnavn: String,
    val dokumentnummer: String,
    val dokumentDatoPrefix: String,
    val dokumentDato: String,
    val tekster: PdfTekster
)

data class PdfTekster(
    val innmelderSeksjonstittel: String,
    val tidOgStedSeksjonstittel: String,
    val skadelidtSeksjonstittel: String,
    val omUlykkenSeksjonstittel: String,
    val omSkadenSeksjonstittel: String,
    val omSkadenFlereSkader: String
)



