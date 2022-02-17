package no.nav.yrkesskade.meldingmottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class ProsesserJournalfoertSkanningTaskMockTest {

    private val safClientMock: SafClient = mockk()

    private val pdlClientMock: PdlClient = mockk(relaxed = true)

    private val oppgaveClientMock: OppgaveClient = mockk(relaxed = true)

    private val journalpostId = "1337"
    private val task = ProsesserJournalfoertSkanningTask.opprettTask(journalpostId)

    private val prosesserJournalfoertSkanningTask: ProsesserJournalfoertSkanningTask =
        ProsesserJournalfoertSkanningTask(safClientMock, pdlClientMock, oppgaveClientMock)


    @Test
    fun `skal kalle paa SAF naar det kommer en kafkarecord`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 1) {safClientMock.hentOppdatertJournalpost(any()) }
    }

    @Test
    fun `skal hente aktoerId fra PDL naar journalpost har foedselsnummer`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerFnr()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 1) {pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal IKKE kalle paa PDL naar journalpost har aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 0) { pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal IKKE lage oppgave naar SAF ikke returnerer en journalpost`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns null

        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }
        assertThat(exception.localizedMessage).isEqualTo("Journalpost med journalpostId $journalpostId finnes ikke i SAF")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave ogsaa naar journalpost fra SAF mangler brukerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenBrukerId()
        prosesserJournalfoertSkanningTask.doTask(task)

        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med foedselsnummer og PDL har aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerFnr()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 1) { pdlClientMock.hentAktorId(any()) }
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar journalstatus paa journalpost fra SAF ikke er MOTTATT`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalstatusFeilregistrert()

        prosesserJournalfoertSkanningTask.doTask(task)

        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar tema paa journalpost fra SAF ikke er YRK`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedTemaSYK()

        prosesserJournalfoertSkanningTask.doTask(task)

        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar journalpost har journalposttype ulik innkommende`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalposttypeUtgaaende()

        prosesserJournalfoertSkanningTask.doTask(task)

        verify(exactly = 0) { pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF mangler dokumenter`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenDokumenter()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).isEqualTo("Journalposten mangler dokumenter.")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF har ugyldig brukerIdType`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedUgyldigBrukerIdType()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).startsWith("BrukerIdType må være en av:")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }
}