package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerFnr
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class JournalfoeringHendelseServiceMockTest {

    private val safClientMock: SafClient = mock()
    private val pdlClientMock: PdlClient = mock()
    private val oppgaveClientMock: OppgaveClient = mock()

    private val record: JournalfoeringHendelseRecord = journalfoeringHendelseRecord()!!

    private val service: JournalfoeringHendelseService = JournalfoeringHendelseService(safClientMock, pdlClientMock, oppgaveClientMock)

    @Test
    fun `skal kalle paa SAF naar det kommer en kafkarecord`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(safClientMock).hentOppdatertJournalpost(any())
    }

    @Test
    fun `skal hente aktoerId fra PDL naar journalpost har foedselsnummer`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock).hentAktorId(any())
    }

    @Test
    fun `skal IKKE kalle paa PDL naar journalpost har aktoerId`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock, never()).hentAktorId(any())
    }

    @Test
    fun `skal IKKE lage oppgave naar SAF ikke returnerer en journalpost`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(null)

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }
        assertThat(exception.localizedMessage).isEqualTo("Journalpost med journalpostId ${record.journalpostId} finnes ikke i SAF")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med aktoerId`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(oppgaveClientMock).opprettOppgave(any())
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med foedselsnummer og PDL har aktoerId`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock).hentAktorId(any())
        verify(oppgaveClientMock).opprettOppgave(any())
    }
}