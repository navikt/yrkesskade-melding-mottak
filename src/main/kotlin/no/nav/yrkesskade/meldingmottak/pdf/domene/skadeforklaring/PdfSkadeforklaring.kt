package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.pdf.domene.*

data class PdfSkadeforklaring(
    val innmelder: PdfInnmelder,
    val skadelidt: PdfSkadelidt,
    val tid: PdfTid,
    val arbeidsbeskrivelse: Soknadsfelt<String>,
    val ulykkesbeskrivelse: Soknadsfelt<String>,
    val fravaer: PdfFravaer,
    val behandler: PdfBehandler,
    val dokumentInfo: PdfDokumentInfo
) : PdfData()

data class PdfInnmelder(
    val norskIdentitetsnummer: Soknadsfelt<String>,
    val navn: Soknadsfelt<String>,
    val innmelderrolle: Soknadsfelt<String?>
)

data class PdfSkadelidt(
    val norskIdentitetsnummer: Soknadsfelt<String>,
    val navn: Soknadsfelt<String>
)

data class PdfFravaer(
    val harFravaer: Soknadsfelt<String>,
    val fravaertype: Soknadsfelt<String>
)

data class PdfBehandler(
    val erBehandlerOppsokt: Soknadsfelt<String>,
    val behandlernavn: Soknadsfelt<String?>,
    val behandleradresse: Soknadsfelt<PdfAdresse?>
)
