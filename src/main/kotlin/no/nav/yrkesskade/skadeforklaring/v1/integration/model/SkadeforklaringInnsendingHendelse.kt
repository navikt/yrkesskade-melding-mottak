package no.nav.yrkesskade.skadeforklaring.v1.integration.model

import no.nav.yrkesskade.skadeforklaring.integration.mottak.ISkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.v1.model.Skadeforklaring

data class SkadeforklaringInnsendingHendelse(
    val metadata: SkadeforklaringMetadata,
    val skadeforklaring: Skadeforklaring
) : ISkadeforklaringInnsendingHendelse