package no.nav.yrkesskade.meldingmottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClient
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClientStub
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.config.FeatureToggleService
import no.nav.yrkesskade.meldingmottak.fixtures.hentIdenterResultMedBrukerAktoeridOgFoedselsnummer
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalposttypeUtgaaende
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedJournalstatusFeilregistrert
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedTemaSYK
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultMedUgyldigBrukerIdType
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultTannlegeerklaering
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenBruker
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenBrukerId
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultUtenDokumenter
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.fixtures.journalpostResultWithBrukerFnr
import no.nav.yrkesskade.meldingmottak.hendelser.DokumentTilSaksbehandlingClient
import no.nav.yrkesskade.meldingmottak.services.RutingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC

internal class ProsesserJournalfoeringHendelseTaskMockTest {

    private val safClientMock: SafClient = mockk()

    private val pdlClientMock: PdlClient = mockk(relaxed = true)

    private val rutingServiceMock: RutingService = mockk()

    private val oppgaveClientMock: OppgaveClient = mockk(relaxed = true)

    private val dokumentTilSaksbehandlingClient: DokumentTilSaksbehandlingClient = mockk(relaxed = true)

    private val bigQueryClientStub: BigQueryClient = BigQueryClientStub()

    private val featureToggleService: FeatureToggleService = mockk()

    private val journalpostId = "1337"
    private val task = ProsesserJournalfoeringHendelseTask.opprettTask(journalpostId)

    private val prosesserJournalfoeringHendelseTask: ProsesserJournalfoeringHendelseTask =
        ProsesserJournalfoeringHendelseTask(safClientMock, pdlClientMock, rutingServiceMock, oppgaveClientMock, bigQueryClientStub, dokumentTilSaksbehandlingClient, featureToggleService)

    @BeforeEach
    fun init() {
        MDC.put(MDCConstants.MDC_CALL_ID, "mock")
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedBrukerAktoeridOgFoedselsnummer()
        every { featureToggleService.isEnabled(any(), any()) } returns true
    }

    @Test
    fun `skal kalle paa SAF naar det kommer en kafkarecord`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 1) {safClientMock.hentOppdatertJournalpost(any()) }
    }

    @Test
    fun `skal hente aktoerId fra PDL naar journalpost har foedselsnummer`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerFnr()

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 1) { pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal IKKE kalle paa PDL naar journalpost har aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 0) { pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal lage oppgave naar tannlegeerklaering kommer inn, men rutes til gammelt saksbehandlingssystem Gosys pga eksisterende sak el,l`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultTannlegeerklaering()
        every { rutingServiceMock.utfoerRuting(any()) } returns RutingService.Rute.GOSYS_OG_INFOTRYGD

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal IKKE lage oppgave naar tannlegeerklaering kommer inn, og record rutes til yrkesskade saksbehandlingssystem`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultTannlegeerklaering()
        every { rutingServiceMock.utfoerRuting(any()) } returns RutingService.Rute.YRKESSKADE_SAKSBEHANDLING

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
        verify(exactly = 1) { dokumentTilSaksbehandlingClient.sendTilSaksbehandling(any()) }
    }

    @Test
    fun `skal IKKE lage oppgave naar SAF ikke returnerer en journalpost`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns null

        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoeringHendelseTask.doTask(task)
        }
        assertThat(exception.localizedMessage).isEqualTo("Journalpost med journalpostId $journalpostId finnes ikke i SAF")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerAktoerid()

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave ogsaa naar journalpost fra SAF mangler brukerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenBrukerId()
        prosesserJournalfoeringHendelseTask.doTask(task)

        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave naar journalpost fra SAF mangler bruker`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenBruker()
        prosesserJournalfoeringHendelseTask.doTask(task)

        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal lage oppgave naar SAF returnerer journalpost med foedselsnummer og PDL har aktoerId`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultWithBrukerFnr()

        prosesserJournalfoeringHendelseTask.doTask(task)
        verify(exactly = 1) { pdlClientMock.hentAktorId(any()) }
        verify(exactly = 1) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar journalstatus paa journalpost fra SAF ikke er MOTTATT`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalstatusFeilregistrert()

        prosesserJournalfoeringHendelseTask.doTask(task)

        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar tema paa journalpost fra SAF ikke er YRK`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedTemaSYK()

        prosesserJournalfoeringHendelseTask.doTask(task)

        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal ikke opprette oppgave naar journalpost har journalposttype ulik innkommende`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedJournalposttypeUtgaaende()

        prosesserJournalfoeringHendelseTask.doTask(task)

        verify(exactly = 0) { pdlClientMock.hentAktorId(any()) }
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF mangler dokumenter`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultUtenDokumenter()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoeringHendelseTask.doTask(task)
        }

        assertThat(exception.localizedMessage).isEqualTo("Journalposten mangler dokumenter.")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }

    @Test
    fun `skal kaste exception naar journalpost fra SAF har ugyldig brukerIdType`() {
        every { safClientMock.hentOppdatertJournalpost(any()) } returns journalpostResultMedUgyldigBrukerIdType()
        val exception = assertThrows(RuntimeException::class.java) {
            prosesserJournalfoeringHendelseTask.doTask(task)
        }

        assertThat(exception.localizedMessage).startsWith("BrukerIdType må være en av:")
        verify(exactly = 0) { oppgaveClientMock.opprettOppgave(any()) }
    }
}