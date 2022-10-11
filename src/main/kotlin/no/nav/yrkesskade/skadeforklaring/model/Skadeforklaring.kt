package no.nav.yrkesskade.skadeforklaring.model

import java.time.LocalDate

data class Skadeforklaring(
    val saksnummer: String?,
    val innmelder: Innmelder,
    val skadelidt: Skadelidt,
    val arbeidetMedIUlykkesoeyeblikket: String,
    val noeyaktigBeskrivelseAvHendelsen: String,
    val tid: Tid,
    val skalEttersendeDokumentasjon: String,
    val vedleggreferanser: List<Vedleggreferanse>,
    val fravaer: Fravaer,
    val helseinstitusjoner: List<Helseinstitusjon>,
    val erHelsepersonellOppsokt: String,
    val foersteHelsepersonellOppsoktDato: LocalDate?
)