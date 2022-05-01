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
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkadeforklaringInnsendingHendelseMedBildevedlegg
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkadeforklaringInnsendingHendelseMedVedlegg
import no.nav.yrkesskade.meldingmottak.vedlegg.Image2PDFConverter
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.storage.Blob
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class SkadeforklaringServiceMockTest {

    private val pdfService: PdfService = mockk()
    private val pdlClient: PdlClient = mockk()
    private val dokarkivClient: DokarkivClient = mockk()
    private val bigQueryClient = BigQueryClientStub()
    private val storageService: StorageService = mockk()
    private val image2PDFConverter: Image2PDFConverter = mockk()



    private val service: SkadeforklaringService = SkadeforklaringService(pdfService, pdlClient, dokarkivClient, bigQueryClient, storageService, image2PDFConverter)

    @BeforeEach
    fun setup() {
        every { pdfService.lagPdf(ofType(SkadeforklaringInnsendingHendelse::class), any()) } answers { ByteArray(10) }
        every { pdfService.lagBeriketPdf(ofType(SkadeforklaringInnsendingHendelse::class), any(), any()) } answers { ByteArray(10) }
        every { pdlClient.hentNavn(any()) } answers { Navn("John", null, "Doe") }
        every { dokarkivClient.journalfoerDokument(any()) } answers { OpprettJournalpostResponse(false, "123", emptyList()) }
        every { storageService.hent(any(), any()) } answers { Blob("10", "12345678901", readPdfFile(), "vedlegg-1.pdf", 250) }
        every { image2PDFConverter.convert(ofType(ByteArray::class)) } answers { readJpegFile() }
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

    @Test
    fun `skal kalle paa storageService naar en skadeforklaring med vedlegg kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelseMedVedlegg())
        verify(exactly = 2) { storageService.hent(any(), any()) }
    }

    @Test
    fun `skal ikke kalle paa storageService naar en skadeforklaring uten vedlegg kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelse())
        verify(exactly = 0) { storageService.hent(any(), any()) }
    }

    @Test
    fun `skal kalle paa image2PDFConverter naar en skadeforklaring med gyldig bildevedlegg kommer inn`() {
        every { storageService.hent(any(), any()) } answers { Blob("10", "12345678901", readJpegFile(), "vedlegg-3.jpeg", 250) }

        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelseMedBildevedlegg())
        verify(exactly = 1) { storageService.hent(any(), any()) }
    }

    @Test
    fun `skal ikke kalle paa image2PDFConverter naar en skadeforklaring med pdf-vedlegg kommer inn`() {
        service.mottaSkadeforklaring(enkelSkadeforklaringInnsendingHendelseMedVedlegg())
        verify(exactly = 2) { storageService.hent(any(), any()) }
    }


    private fun readPdfFile(): ByteArray =
        ClassPathResource("pdf/vedlegg-1.pdf").file.readBytes()

    private fun readJpegFile(): ByteArray =
        ClassPathResource("pdf/vedlegg-3.jpeg").file.readBytes()

}