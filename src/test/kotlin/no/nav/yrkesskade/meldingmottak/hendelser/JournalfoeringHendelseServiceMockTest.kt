package no.nav.yrkesskade.meldingmottak.hendelser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedKanalNAVNO
import no.nav.yrkesskade.meldingmottak.fixtures.journalfoeringHendelseRecordMedTemaSYK
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoertSkanningTask
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JournalfoeringHendelseServiceMockTest {

    private val taskRepository: TaskRepository = mockk()

    private val service: JournalfoeringHendelseService = JournalfoeringHendelseService(taskRepository)

    @BeforeEach
    fun setup() {
        every { taskRepository.save(any()) } returns ProsesserJournalfoertSkanningTask.opprettTask("123")
    }

    @Test
    fun `skal kalle paa taskRepository naar en relevant record kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecord()!!)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `skal ikke kalle paa taskRepository naar en record med tema ulikt YRK kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedTemaSYK())
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `skal kalle paa taskRepository naar en record med kanal som ikke begynner paa SKAN_ kommer inn`() {
        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecordMedKanalNAVNO())
        verify(exactly = 0) { taskRepository.save(any()) }
    }
}