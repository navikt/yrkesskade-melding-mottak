package no.nav.yrkesskade.skadeforklaring.model

data class Skadeforklaring(
    val saksnummer: String?,
    val innmelder: Innmelder?,
    val skadelidt: Skadelidt?,
    val arbeidetMedIUlykkesoeyeblikket: String,
    val noeyaktigBeskrivelseAvHendelsen: String,
    val tid: Tid,
    val skalEttersendeDokumentasjon: String,
    val vedleggreferanser: List<Vedleggreferanse>,
    val fravaer: Fravaer,
    val helseinstitusjon: Helseinstitusjon
)