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
    fun `should call saf`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(safClientMock).hentOppdatertJournalpost(any())
    }

    @Test
    fun `should get AKTORID from pdl when fødselsnummer in journalpost`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock).hentAktorId(any())
    }

    @Test
    fun `should NOT call pdl when aktørID in journalpost`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock, never()).hentAktorId(any())
    }

    @Test()
    fun `should NOT create oppgave when journalpost not in saf`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(null)

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }
        assertThat(exception.localizedMessage).isEqualTo("Journalpost med journalpostId ${record.journalpostId} finnes ikke i SAF")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `should create oppgave when journalpost with aktoerid found in saf`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        service.prosesserJournalfoeringHendelse(record)
        verify(oppgaveClientMock).opprettOppgave(any())
    }

    @Test
    fun `should create oppgave when journalpost found in saf and aktørID found in pdl`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        service.prosesserJournalfoeringHendelse(record)
        verify(pdlClientMock).hentAktorId(any())
        verify(oppgaveClientMock).opprettOppgave(any())
    }
}