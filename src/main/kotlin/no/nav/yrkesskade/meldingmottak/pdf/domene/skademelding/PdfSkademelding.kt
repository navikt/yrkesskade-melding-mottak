package no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding

import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfAdresse
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.Soknadsfelt

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
    val virksomhetensAdresse: Soknadsfelt<PdfAdresse?>,
    val stillingstittelTilDenSkadelidte: Soknadsfelt<List<String>>,
    val rolletype: Soknadsfelt<PdfRolletype>
)

data class PdfRolletype(
    val kode: String,
    val navn: String,
)

data class PdfSkade(
    val alvorlighetsgrad: Soknadsfelt<String?>,
    val skadedeDeler: List<PdfSkadetDel>,
    val antattSykefravaer: Soknadsfelt<String>
)

data class PdfSkadetDel(
    val kroppsdel: Soknadsfelt<String>,
    val skadeart: Soknadsfelt<String>
)

data class PdfHendelsesfakta(
    val tid: PdfTid,
    val naarSkjeddeUlykken: Soknadsfelt<String>,
    val hvorSkjeddeUlykken: Soknadsfelt<String>,
    val ulykkessted: PdfUlykkessted,
    val paavirkningsform: Soknadsfelt<List<String>?>,
    val aarsakUlykke: Soknadsfelt<List<String>>,
    val bakgrunnsaarsak: Soknadsfelt<List<String>>,
    val stedsbeskrivelse: Soknadsfelt<String?>,
    val utfyllendeBeskrivelse: Soknadsfelt<String?>
)

data class PdfUlykkessted(
    val sammeSomVirksomhetensAdresse: Soknadsfelt<String>,
    val adresse: Soknadsfelt<PdfAdresse?>
)

data class PdfTid(
    val tidstype: String,
    val tidspunkt: Soknadsfelt<PdfTidspunkt>,
    val perioder: Soknadsfelt<List<PdfPeriode>?>,
    val sykdomPaavist: Soknadsfelt<String?>,
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

data class PdfDokumentInfo(
    val dokumentnavn: String,
    val dokumentnummer: String,
    val dokumentDatoPrefix: String,
    val dokumentDato: String,
    val tekster: PdfTekster,
    val annet: PdfAnnet
)

data class PdfTekster(
    val innmelderSeksjonstittel: String,
    val tidOgStedSeksjonstittel: String,
    val skadelidtSeksjonstittel: String,
    val omUlykkenSeksjonstittel: String,
    val omSkadenSeksjonstittel: String,
    val omSkadenFlereSkader: String
)

data class PdfAnnet(
    val erSykdom: Boolean
)
