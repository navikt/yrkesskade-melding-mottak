package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClientStub
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkadeforklaringServiceMockTest {

    private val pdfService: PdfService = mockk()
    private val pdlClient: PdlClient = mockk()
    private val dokarkivClient: DokarkivClient = mockk()
    private val bigQueryClient = BigQueryClientStub()

    private val service: SkadeforklaringService = SkadeforklaringService(pdfService, pdlClient, dokarkivClient, bigQueryClient)

    @BeforeEach
    fun setup() {
        every { pdfService.lagPdf(ofType(SkadeforklaringInnsendingHendelse::class), any()) } answers { ByteArray(10) }
        every { pdfService.lagBeriketPdf(ofType(SkadeforklaringInnsendingHendelse::class), any(), any()) } answers { ByteArray(10) }
        every { pdlClient.hentNavn(any()) } answers { Navn("John", null, "Doe") }
        every { dokarkivClient.journalfoerDokument(any()) } answers { OpprettJournalpostResponse(false, "123", emptyList()) }
    }

    @Test
    fun `skal kalle paa pdfService naar en skadeforklaring kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelse())
        verify(exactly = 1) { pdfService.lagPdf(ofType(SkadeforklaringInnsendingHendelse::class), any()) }
        verify(exactly = 1) { pdfService.lagBeriketPdf(ofType(SkadeforklaringInnsendingHendelse::class), any(), any()) }
    }

    @Test
    fun `skal kalle paa pdlClient naar en skadeforklaring kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelse())
        verify(exactly = 2) { pdlClient.hentNavn(any()) }
    }

    @Test
    fun `skal kalle paa dokarkivClient naar en skadeforklaring kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelse())
        verify(exactly = 1) { dokarkivClient.journalfoerDokument(any()) }
    }
}