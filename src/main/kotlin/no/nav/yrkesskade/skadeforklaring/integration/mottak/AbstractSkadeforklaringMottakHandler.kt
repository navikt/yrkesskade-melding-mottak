package no.nav.yrkesskade.skadeforklaring.integration.mottak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.*
import no.nav.yrkesskade.meldingmottak.konstanter.TEMA_YRKESSKADE
import no.nav.yrkesskade.meldingmottak.konstanter.TITTEL_DIGITAL_SKADEFORKLARING
import no.nav.yrkesskade.meldingmottak.konstanter.TITTEL_DIGITAL_SKADEFORKLARING_ARKIV
import no.nav.yrkesskade.meldingmottak.services.PdfService
import no.nav.yrkesskade.meldingmottak.services.StorageService
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.meldingmottak.vedlegg.Image2PDFConverter
import no.nav.yrkesskade.meldingmottak.vedlegg.VedleggUtil
import no.nav.yrkesskade.skadeforklaring.v1.model.Vedleggreferanse
import no.nav.yrkesskade.storage.Blob
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.lang.invoke.MethodHandles
import java.time.Instant

abstract class AbstractSkadeforklaringMottakHandler<T : ISkadeforklaringInnsendingHendelse>(
    val pdfService: PdfService,
    val pdlClient: PdlClient,
    val storageService: StorageService,
    val image2PDFConverter: Image2PDFConverter
    ) : SkadeforklaringMottakHandler<T> {

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    val secureLogger = getSecureLogger()

    fun hentNavnFraPersondataloesningen(fodselsnummer: String?): Navn? {
        if (fodselsnummer == null) {
            return null
        }
        return pdlClient.hentNavn(fodselsnummer)
    }

    fun <S> mapSkadeforklaringTilOpprettJournalpostRequest(
        originalData: S,
        tidspunktMottatt: Instant,
        navCallId: String,
        beriketData: BeriketData,
        pdf: ByteArray,
        beriketPdf: ByteArray,
        vedleggdokumenter: List<Dokument>
    ): OpprettJournalpostRequest {
        val skadeforklaringJson = objectMapper.writeValueAsString(originalData)

        return OpprettJournalpostRequest(
            tittel = TITTEL_DIGITAL_SKADEFORKLARING,
            journalfoerendeEnhet = null,
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                navn = beriketData.innmeldersNavn.toString(),
                id = beriketData.innmeldersNorskIdentitetsnummer ?: "",
                idType = BrukerIdType.FNR
            ),
            bruker = Bruker(
                id = beriketData.skadelidtsNorskIdentitetsnummer ?: "",
                idType = BrukerIdType.FNR
            ),
            tema = TEMA_YRKESSKADE,
            kanal = Kanal.NAV_NO.toString(),
            datoMottatt = tidspunktMottatt.toString(),
            eksternReferanseId = navCallId,
            dokumenter = listOf(
                Dokument(
                    brevkode = Brevkode.DIGITAL_SKADEFORKLARING.kode,
                    tittel = TITTEL_DIGITAL_SKADEFORKLARING,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.PDFA,
                            variantformat = Dokumentvariantformat.ARKIV,
                            fysiskDokument = beriketPdf
                        )
                    )
                ),
                Dokument(
                    brevkode = Brevkode.DIGITAL_SKADEFORKLARING.kode,
                    tittel = TITTEL_DIGITAL_SKADEFORKLARING_ARKIV,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.JSON,
                            variantformat = Dokumentvariantformat.ORIGINAL,
                            fysiskDokument = skadeforklaringJson.encodeToByteArray()
                        ),
                        Dokumentvariant(
                            filtype = Filtype.PDFA,
                            variantformat = Dokumentvariantformat.ARKIV,
                            fysiskDokument = pdf
                        ),
                    )
                )
            ) + vedleggdokumenter
        )
    }

    fun opprettBlobForManglendeVedlegg(
        vedleggreferanseNavn: String,
        dokumentEierIdentifikator: String
    ): Blob {
        log.warn("Vedlegg ${vedleggreferanseNavn} finnes ikke! Oppretter et 'dummy' vedlegg med opprinnelig filnavn og melding til saksbehandler.")
        return Blob(
            id = "",
            bruker = dokumentEierIdentifikator,
            bytes = vedleggManglerPdfFil(),
            navn = vedleggreferanseNavn + VedleggUtil.VEDLEGG_MANGLER_MELDING,
            storrelse = vedleggManglerPdfFil().size.toLong()
        )
    }

    private fun vedleggManglerPdfFil(): ByteArray =
        ClassPathResource("pdf/vedlegg-mangler.pdf").inputStream.readAllBytes()


}