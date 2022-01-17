package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalposttypeUtgaaende
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalstatusFeilregistrert
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedTemaSYK
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedUgyldigBrukerIdType
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenBrukerId
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenDokumenter
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerFnr
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

    @Test
    fun `skal kaste exception naar journalstatus paa journalpost fra SAF ikke er MOTTATT`() {
        val journalpostMedUgyldigStatus = journalpostResultMedJournalstatusFeilregistrert()
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostMedUgyldigStatus)
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).startsWith("Journalstatus må være")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `skal kaste exception naar tema paa journalpost fra SAF ikke er YRK`() {
        val journalpostMedUgyldigTema = journalpostResultMedTemaSYK()
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostMedUgyldigTema)
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).startsWith("Journalpostens tema må være")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `skal kaste exception naar journalpost har journalposttype`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultMedJournalposttypeUtgaaende())

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).startsWith("Journalpostens type må være")
        verify(pdlClientMock, never()).hentAktorId(any())
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF mangler dokumenter`() {
        val journalpostUtenDokumenter = journalpostResultUtenDokumenter()
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostUtenDokumenter)
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).isEqualTo("Journalposten mangler dokumenter.")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `skal kaste exception naarjournalpost fra SAF mangler brukerId`() {
        val journalpostUtenBrukerId = journalpostResultUtenBrukerId()
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostUtenBrukerId)
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).isEqualTo("Journalposten mangler brukerId.")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF har ugyldig brukerIdType`() {
        val journalpostMedUgyldigBrukerIdType = journalpostResultMedUgyldigBrukerIdType()
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostMedUgyldigBrukerIdType)
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            service.prosesserJournalfoeringHendelse(record)
        }

        assertThat(exception.localizedMessage).startsWith("BrukerIdType må være en av:")
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }
}