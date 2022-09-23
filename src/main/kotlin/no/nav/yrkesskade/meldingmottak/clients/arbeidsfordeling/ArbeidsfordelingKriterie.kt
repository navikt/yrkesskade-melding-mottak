package no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling

data class ArbeidsfordelingKriterie(
    val tema: String = "YRK",
    val geografiskOmraade: String? = null,
    val diskresjonskode: String? = null,
    val skjermet: Boolean
)
