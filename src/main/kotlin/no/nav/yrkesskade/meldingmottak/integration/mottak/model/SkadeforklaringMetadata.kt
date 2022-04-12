package no.nav.yrkesskade.meldingmottak.integration.mottak.model

import java.time.Instant

data class SkadeforklaringMetadata(
    val tidspunktMottatt: Instant,
    val spraak: Spraak,
    val navCallId: String,
)

enum class Spraak {
    NB, NN, EN
}