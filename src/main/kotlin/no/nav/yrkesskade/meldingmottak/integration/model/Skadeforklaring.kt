package no.nav.yrkesskade.meldingmottak.integration.model

data class Skadeforklaring(
    val saksnummer: String?,
    val innmelder: Innmelder?,
    val skadelidt: Skadelidt?,
    val arbeidetMedIUlykkesoeyeblikket: String,
    val noeyaktigBeskrivelseAvHendelsen: String,
    val tid: Tid,
    val vedleggtype: String,
    val vedleggreferanser: List<Vedleggreferanse>,
    val fravaer: Fravaer,
    val behandler: Behandler
)