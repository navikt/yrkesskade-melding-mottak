package no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding

import no.nav.yrkesskade.meldingmottak.pdf.domene.*

data class PdfSkademelding(
    val innmelder: PdfInnmelder?,
    val skadelidt: PdfSkadelidt?,
    val skade: PdfSkade?,
    val hendelsesfakta: PdfHendelsesfakta?,
    val dokumentInfo: PdfDokumentInfo
) : PdfData()



data class PdfInnmelder(
    val norskIdentitetsnummer: Soknadsfelt<String>,
    val navn: Soknadsfelt<String>,
    val paaVegneAv: Soknadsfelt<String>,
    val innmelderrolle: Soknadsfelt<String>, // ikke i bruk?
    val altinnrolleIDer: Soknadsfelt<List<String>?>
)

data class PdfSkadelidt(
    val norskIdentitetsnummer: Soknadsfelt<String>,
    val navn: Soknadsfelt<String>,
    val bostedsadresse: Soknadsfelt<PdfAdresse>,
    val dekningsforhold: PdfDekningsforhold
)

data class PdfDekningsforhold(
    val organisasjonsnummer: Soknadsfelt<String>,
    val navnPaaVirksomheten: Soknadsfelt<String?>,
    val stillingstittelTilDenSkadelidte: Soknadsfelt<List<String>>,
    val rolletype: Soknadsfelt<String>
)

data class PdfSkade(
    val alvorlighetsgrad: Soknadsfelt<String?>,
    val skadedeDeler: List<PdfSkadetDel>,
    val antattSykefravaerTabellH: Soknadsfelt<String>
)

data class PdfSkadetDel(
    val kroppsdelTabellD: Soknadsfelt<String>,
    val skadeartTabellC: Soknadsfelt<String>
)

data class PdfHendelsesfakta(
    val tid: PdfTid,
    val naarSkjeddeUlykken: Soknadsfelt<String>,
    val hvorSkjeddeUlykken: Soknadsfelt<String>,
    val ulykkessted: PdfUlykkessted,
    val aarsakUlykkeTabellAogE: Soknadsfelt<List<String>>,
    val bakgrunnsaarsakTabellBogG: Soknadsfelt<List<String>>,
    val stedsbeskrivelseTabellF: Soknadsfelt<String>,
    val utfyllendeBeskrivelse: Soknadsfelt<String?>
)

data class PdfUlykkessted(
    val sammeSomVirksomhetensAdresse: Soknadsfelt<String>,
    val adresse: Soknadsfelt<PdfAdresse?>
)

