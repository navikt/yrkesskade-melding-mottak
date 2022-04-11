package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClientStub
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.Adresse
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkademeldingServiceMockTest {

    private val pdfService: PdfService = mockk()
    private val pdlClient: PdlClient = mockk()
    private val dokarkivClient: DokarkivClient = mockk()
    private val bigQueryClient = BigQueryClientStub()

    private val service: SkademeldingService = SkademeldingService(pdfService, pdlClient, dokarkivClient, bigQueryClient)

    @BeforeEach
    fun setup() {
        every { pdfService.lagPdf(ofType(SkademeldingInnsendtHendelse::class), any()) } answers { ByteArray(10) }
        every { pdfService.lagBeriketPdf(ofType(SkademeldingInnsendtHendelse::class), any(), any()) } answers { ByteArray(10) }
        every { pdlClient.hentNavn(any()) } answers { Navn("John", null, "Doe") }
        every { pdlClient.hentNavnOgAdresse(any(), any()) } answers {
            Pair(
                Navn("Kari", "Bull", "Hansen"),
                Adresse("Liaveien 3B", "1250 Plassen", "", ""))
        }
        every { dokarkivClient.journalfoerSkademelding(any()) } answers { OpprettJournalpostResponse(false, "123", emptyList()) }
    }

    @Test
    fun `skal kalle paa pdfService naar en skademelding kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 1) { pdfService.lagPdf(ofType(SkademeldingInnsendtHendelse::class), any()) }
        verify(exactly = 1) { pdfService.lagBeriketPdf(ofType(SkademeldingInnsendtHendelse::class), any(), any()) }
    }

    @Test
    fun `skal kalle paa pdlClient naar en skademelding kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 1) { pdlClient.hentNavn(any()) }
        verify(exactly = 1) { pdlClient.hentNavnOgAdresse(any(), any()) }
    }

    @Test
    fun `skal kalle paa dokarkivClient naar en skademelding kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 1) { dokarkivClient.journalfoerSkademelding(any()) }
    }
}