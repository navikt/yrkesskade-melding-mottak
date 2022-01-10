package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerAktoerid
import no.nav.yrkesskade.meldingmottak.hendelser.fixtures.journalpostResultWithBrukerFnr
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class JournalfoeringHendelseConsumerMockTest {

    private val safClientMock: SafClient = mock()
    private val pdlClientMock: PdlClient = mock()
    private val oppgaveClientMock: OppgaveClient = mock()

    private val record: JournalfoeringHendelseRecord = journalfoeringHendelseRecord()!!

    private val consumer: JournalfoeringHendelseConsumer = JournalfoeringHendelseConsumer(safClientMock, pdlClientMock, oppgaveClientMock)



    @Test
    @Disabled
    fun `should call saf`() {
        consumer.listen(record)
        verify(safClientMock).hentOppdatertJournalpost(any())
    }

    @Test
    @Disabled
    fun `should get AKTORID from pdl when fødselsnummer in journalpost`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        consumer.listen(record)
        verify(pdlClientMock).hentAktorId(any())
    }

    @Test
    @Disabled
    fun `should NOT call pdl when aktørID in journalpost`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        consumer.listen(record)
        verify(pdlClientMock, never()).hentAktorId(any())
    }

    @Test
    @Disabled
    fun `should NOT create oppgave when journalpost not in saf`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(null)

        consumer.listen(record)
        verify(oppgaveClientMock, never()).opprettOppgave(any())
    }

    @Test
    @Disabled
    fun `should create oppgave when journalpost with aktoerid found in saf`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerAktoerid())

        consumer.listen(record)
        verify(oppgaveClientMock).opprettOppgave(any())
    }

    @Test
    @Disabled
    fun `should create oppgave when journalpost found in saf and aktørID found in pdl`() {
        `when`(safClientMock.hentOppdatertJournalpost(any())).thenReturn(journalpostResultWithBrukerFnr())

        consumer.listen(record)
        verify(pdlClientMock).hentAktorId(any())
        verify(oppgaveClientMock).opprettOppgave(any())
    }
}