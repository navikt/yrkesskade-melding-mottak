package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerFnr
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class JournalfoeringHendelseConsumerMockTest {

    private val safClientMock: SafClient = mock()
    private val oppgaveClientMock: OppgaveClient = mock()

    private val record: JournalfoeringHendelseRecord = journalfoeringHendelseRecord()!!

    private val consumer: JournalfoeringHendelseConsumer = JournalfoeringHendelseConsumer(safClientMock, oppgaveClientMock)



    @Test
    fun should_call_saf() {
        consumer.listen(record)
        verify(safClientMock).hentOppdatertJournalpost(any())
    }

    @Test
    fun should_NOT_create_oppgave_when_journalpost_not_in_saf() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(null)

        consumer.listen(record)
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun should_NOT_create_oppgave_when_journalpost_with_fnr_found_in_saf() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        consumer.listen(record)
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun should_create_oppgave_when_journalpost_with_aktoerid_found_in_saf() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        consumer.listen(record)
        verify(oppgaveClientMock).opprettOppgave(any())
    }
}