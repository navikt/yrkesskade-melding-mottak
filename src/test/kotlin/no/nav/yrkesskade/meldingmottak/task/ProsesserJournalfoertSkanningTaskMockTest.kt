package no.nav.yrkesskade.meldingmottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalposttypeUtgaaende
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalstatusFeilregistrert
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedTemaSYK
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedUgyldigBrukerIdType
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenBrukerId
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenDokumenter
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerFnr
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
    fun `skal lage oppgave naar SAF returnerer journalpost med foedselsnummer og PDL har aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerFnr()

        prosesserJournalfoertSkanningTask.doTask(task)
        verify(exactly = 1) { pdlClientMock.hentAktorId(any()) }
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal kaste exception naar journalstatus paa journalpost fra SAF ikke er MOTTATT`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalstatusFeilregistrert()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).startsWith("Journalstatus må være")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal kaste exception naar tema paa journalpost fra SAF ikke er YRK`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedTemaSYK()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).startsWith("Journalpostens tema må være")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal kaste exception naar journalpost har journalposttype ulik innkommende`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalposttypeUtgaaende()

        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).startsWith("Journalpostens type må være")
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
    fun `skal kaste exception naarjournalpost fra SAF mangler brukerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenBrukerId()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoertSkanningTask.doTask(task)
        }

        assertThat(exception.localizedMessage).isEqualTo("Journalposten mangler brukerId.")
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