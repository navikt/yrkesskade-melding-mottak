package no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring

import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfAdresse
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.Soknadsfelt

data class PdfSkadeforklaringV2(
    val innmelder: PdfInnmelderV2,
    val skadelidt: PdfSkadelidtV2,
    val tid: PdfTidV2,
    val arbeidetMedIUlykkesoeyeblikket: Soknadsfelt<String>,
    val noeyaktigBeskrivelseAvHendelsen: Soknadsfelt<String>,
    val fravaer: PdfFravaerV2,
    val helseinstitusjoner: List<PdfHelseinstitusjonV2>,
    val vedleggInfo: Soknadsfelt<List<String>>,
    val dokumentInfo: PdfDokumentInfoSkadeforklaringV2,
    val erHelsepersonellOppsokt: Soknadsfelt<String>,
) : PdfData()

data class PdfInnmelderV2(
    val norskIdentitetsnummer: Soknadsfelt<String?>,
    val navn: Soknadsfelt<String?>,
    val innmelderrolle: Soknadsfelt<String?>
)

data class PdfHelsepersonellOppsoktV2(
    val erHelsepersonellOppsokt: Soknadsfelt<String>,
)

data class PdfSkadelidtV2(
    val norskIdentitetsnummer: Soknadsfelt<String?>,
    val navn: Soknadsfelt<String?>
)

data class PdfFravaerV2(
    val foerteDinSkadeEllerSykdomTilFravaer: Soknadsfelt<String>,
    val fravaertype: Soknadsfelt<String?>
)

data class PdfHelseinstitusjonV2(
    val navn: Soknadsfelt<String?>
)

data class PdfDokumentInfoSkadeforklaringV2(
    val dokumentnavn: String,
    val dokumentnummer: String,
    val dokumentDatoPrefix: String,
    val dokumentDato: String,
    val tekster: PdfTeksterSkadeforklaringV2
)

data class PdfTeksterSkadeforklaringV2(
    val innmelderSeksjonstittel: String,
    val tidOgStedSeksjonstittel: String,
    val skadelidtSeksjonstittel: String,
    val omUlykkenSeksjonstittel: String,
    val omSkadenSeksjonstittel: String,
    val vedleggSeksjonstittel: String
)

data class PdfTidV2(
    val tidstype: String,
    val tidspunkt: Soknadsfelt<PdfTidspunktV2>,
    val periode: Soknadsfelt<PdfPeriodeV2>,
    val ukjent: Soknadsfelt<Boolean?>
)

data class PdfTidspunktV2(
    val dato: String,
    val klokkeslett: String
)

data class PdfPeriodeV2(
    val fra: String,
    val til: String
)
