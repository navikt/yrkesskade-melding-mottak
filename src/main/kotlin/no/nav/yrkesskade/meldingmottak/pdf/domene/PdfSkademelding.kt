package no.nav.yrkesskade.meldingmottak.pdf.domene

data class Soknadsfelt<T>(
    val label: String,
    val verdi: T
)


data class PdfSkademelding(
    val innmelder: PdfInnmelder?,
    val skadelidt: PdfSkadelidt?,
    val skade: PdfSkade?,
    val hendelsesfakta: PdfHendelsesfakta?,
    val dokumentInfo: PdfDokumentInfo
)



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

data class PdfTid(
    val tidstype: String,
    val tidspunkt: Soknadsfelt<PdfTidspunkt>,
    val periode: Soknadsfelt<PdfPeriode>,
    val ukjent: Boolean?
)

data class PdfTidspunkt(
    val dato: String,
    val klokkeslett: String
)

data class PdfPeriode(
    val fra: String,
    val til: String
)

data class PdfUlykkessted(
    val sammeSomVirksomhetensAdresse: Soknadsfelt<String>,
    val adresse: Soknadsfelt<PdfAdresse?>
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