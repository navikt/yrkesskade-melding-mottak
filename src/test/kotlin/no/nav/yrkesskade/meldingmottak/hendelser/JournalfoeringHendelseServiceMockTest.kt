package no.nav.yrkesskade.meldingmottak.hendelser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveResponse
import no.nav.yrkesskade.meldingmottak.fixtures.enkelOppgave
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedJournalpoststatusJOURNALFOERT
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedKanalALTINN
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedKanalNAVNO
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedKanalSKAN_NETS
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedTemaSYK
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoeringHendelseTask
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JournalfoeringHendelseServiceMockTest {

    private val taskRepository: TaskRepository = mockk()
    private val oppgaveClient: OppgaveClient = mockk()

    private val service: JournalfoeringHendelseService = JournalfoeringHendelseService(taskRepository, oppgaveClient)

    @BeforeEach
    fun setup() {
        every { taskRepository.save(any()) } returns ProsesserJournalfoeringHendelseTask.opprettTask("123")
        every { oppgaveClient.finnOppgaver(any(), any()) } returns OppgaveResponse(0, emptyList())
    }

    @Test
    fun `skal kalle paa taskRepository naar en record med tema YRK og kanal SKAN_IM kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecord()!!)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `skal kalle paa taskRepository naar en record med tema YRK og kanal NAV_NO kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedKanalNAVNO())
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `skal kalle paa taskRepository naar en record med tema YRK og kanal SKAN_NETS kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedKanalSKAN_NETS())
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `skal ikke kalle paa taskRepository naar en record med tema ulikt YRK kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedTemaSYK())
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `skal ikke kalle paa taskRepository naar en record med kanal vi ikke lytter paa kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedKanalALTINN())
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `skal ikke kalle paa taskRepository naar en record med journalpoststatus vi ikke lytter paa kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedJournalpoststatusJOURNALFOERT())
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `skal ikke kalle paa taskRepository naar en oppgave finnes fra foer`() {
        every { oppgaveClient.finnOppgaver(any(), any()) } returns OppgaveResponse(1, listOf(enkelOppgave()))

        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedKanalSKAN_NETS())
        verify(exactly = 0) { taskRepository.save(any()) }
    }
}