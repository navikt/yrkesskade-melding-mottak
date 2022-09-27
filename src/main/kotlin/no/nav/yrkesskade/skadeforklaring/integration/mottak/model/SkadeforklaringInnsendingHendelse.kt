package no.nav.yrkesskade.skadeforklaring.integration.mottak.model

import no.nav.yrkesskade.skadeforklaring.model.Skadeforklaring

data class SkadeforklaringInnsendingHendelse(
    val metadata: SkadeforklaringMetadata,
    val skadeforklaring: Skadeforklaring
)