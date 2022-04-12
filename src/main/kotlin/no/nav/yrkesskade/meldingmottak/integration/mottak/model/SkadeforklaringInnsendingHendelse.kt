package no.nav.yrkesskade.meldingmottak.integration.mottak.model

import no.nav.yrkesskade.meldingmottak.integration.model.Skadeforklaring

data class SkadeforklaringInnsendingHendelse(
    val metadata: SkadeforklaringMetadata,
    val skadeforklaring: Skadeforklaring
)