package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkademeldingServiceMockTest {

    private val pdfService: PdfService = mockk()
    private val dokarkivClient: DokarkivClient = mockk()

    private val service: SkademeldingService = SkademeldingService(pdfService, dokarkivClient)

    @BeforeEach
    fun setup() {
        every { pdfService.lagPdf(any(), any()) } answers { ByteArray(10) }
        every { dokarkivClient.journalfoerSkademelding(any()) } answers { OpprettJournalpostResponse(false, "123", emptyList()) }
    }

    @Test
    fun `skal kalle 2 ganger paa pdfService naar en skademelding kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 2) { pdfService.lagPdf(any(), any()) }
    }

    @Test
    fun `skal kalle paa dokarkivClient naar en skademelding kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 1) { dokarkivClient.journalfoerSkademelding(any()) }
    }
}