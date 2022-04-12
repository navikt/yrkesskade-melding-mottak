package no.nav.yrkesskade.meldingmottak.integration.model

data class Skadeforklaring(
    val innmelder: Innmelder,
    val skadelidt: Skadelidt,
    val arbeidsbeskrivelse: String,
    val ulykkesbeskrivelse: String,
    val tid: Tid,
    val vedleggtype: String,
    val vedleggreferanser: List<Vedleggreferanse>,
    val fravaer: Fravaer,
    val behandler: Behandler
)