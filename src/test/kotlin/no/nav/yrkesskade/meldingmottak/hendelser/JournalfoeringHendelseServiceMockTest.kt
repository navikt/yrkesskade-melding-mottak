package no.nav.yrkesskade.meldingmottak.hendelser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveResponse
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.fixtures.*
import no.nav.yrkesskade.meldingmottak.services.JournalfoeringHendelseService
import no.nav.yrkesskade.meldingmottak.services.RutingService
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoeringHendelseTask
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JournalfoeringHendelseServiceMockTest {

    private val taskRepository: TaskRepository = mockk()
    private val oppgaveClient: OppgaveClient = mockk()
    private val safClient: SafClient = mockk()
    private val pdlClient: PdlClient = mockk()
    private val rutingService: RutingService = mockk()

    private val service: JournalfoeringHendelseService = JournalfoeringHendelseService(taskRepository, oppgaveClient, safClient, pdlClient, rutingService)

    @BeforeEach
    fun setup() {
        every { taskRepository.save(any()) } returns ProsesserJournalfoeringHendelseTask.opprettTask("123")
        every { oppgaveClient.finnOppgaver(any(), any()) } returns OppgaveResponse(0, emptyList())
        every { safClient.hentOppdatertJournalpost(any()) } returns okJournalpostRespons().data
        every { pdlClient.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedBrukerAktoeridOgFoedselsnummer()
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
    fun `skal kalle paa taskrepository naar tannlegeerklaering kommer inn men record rutes til gammelt saksbehandlingssystem Gosys pga eksisterende sak el,l`() {
        every { safClient.hentOppdatertJournalpost(any()) } returns journalpostResultTannlegeerklaering()
        every { rutingService.utfoerRuting(any()) } returns RutingService.Rute.GOSYS_OG_INFOTRYGD

        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecord()!!)
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

    @Test
    fun `skal ikke kalle paa taskrepository naar tannlegeerklaering kommer inn og record rutes til yrkesskade saksbehandlingssystem`() {
        every { safClient.hentOppdatertJournalpost(any()) } returns journalpostResultTannlegeerklaering()
        every { rutingService.utfoerRuting(any()) } returns RutingService.Rute.YRKESSKADE_SAKSBEHANDLING

        service.prosesserJournalfoeringHendelse(journalfoeringHendelseRecord()!!)
//        verify(exactly = 0) { taskRepository.save(any()) }
        verify(exactly = 1) { taskRepository.save(any()) } // TODO: YSMOD-370 Lar meldinger midlertidig gå til gammel saksbehandlingsløsning i Gosys/Infotrygd
//        verify(exactly = 1) { sendTilSaksbehandlingClient.send(any()) }
    }

}