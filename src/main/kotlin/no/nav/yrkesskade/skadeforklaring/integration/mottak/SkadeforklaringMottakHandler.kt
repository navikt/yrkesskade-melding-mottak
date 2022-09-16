package no.nav.yrkesskade.skadeforklaring.integration.mottak

import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostRequest

interface SkadeforklaringMottakHandler<T : ISkadeforklaringInnsendingHendelse> {
    fun motta(record: T) : OpprettJournalpostRequest
}