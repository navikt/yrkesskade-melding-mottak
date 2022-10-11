package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.Soknadsfelt

data class PdfSkadeforklaring(
    val innmelder: PdfInnmelder,
    val skadelidt: PdfSkadelidt,
    val tid: PdfTid,
    val arbeidetMedIUlykkesoeyeblikket: Soknadsfelt<String>,
    val noeyaktigBeskrivelseAvHendelsen: Soknadsfelt<String>,
    val fravaer: PdfFravaer,
    val erHelsepersonellOppsokt: Soknadsfelt<String>,
    val foersteHelsepersonellOppsoktDato: Soknadsfelt<String>,
    val helseinstitusjoner: Soknadsfelt<List<PdfHelseinstitusjon>>,
    val vedleggInfo: Soknadsfelt<List<String>>,
    val dokumentInfo: PdfDokumentInfoSkadeforklaring
) : PdfData()

data class PdfInnmelder(
    val norskIdentitetsnummer: Soknadsfelt<String?>,
    val navn: Soknadsfelt<String?>,
    val innmelderrolle: Soknadsfelt<String?>
)

data class PdfSkadelidt(
    val norskIdentitetsnummer: Soknadsfelt<String?>,
    val navn: Soknadsfelt<String?>
)

data class PdfFravaer(
    val foerteDinSkadeEllerSykdomTilFravaer: Soknadsfelt<String>,
    val fravaertype: Soknadsfelt<String?>
)

data class PdfHelseinstitusjon(
    val navn: String?,
)

data class PdfDokumentInfoSkadeforklaring(
    val dokumentnavn: String,
    val dokumentnummer: String,
    val dokumentDatoPrefix: String,
    val dokumentDato: String,
    val tekster: PdfTeksterSkadeforklaring
)

data class PdfTeksterSkadeforklaring(
    val innmelderSeksjonstittel: String,
    val tidOgStedSeksjonstittel: String,
    val skadelidtSeksjonstittel: String,
    val omUlykkenSeksjonstittel: String,
    val omSkadenSeksjonstittel: String,
    val vedleggSeksjonstittel: String
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
