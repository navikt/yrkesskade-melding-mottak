package no.nav.yrkesskade.meldingmottak.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClient
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.*
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

private const val TEMA_YRKESSKADE = "YRK"

private const val DIGITAL_SKADEFORKLARING_ARKIV_TITTEL = "Arkivlogg fra innsending"

private const val DIGITAL_SKADEFORKLARING_TITTEL = "Skadeforklaring ved arbeidsulykke"

private const val DIGITAL_SKADEFORKLARING_BREVKODE = "NAV 13-00.21"

@Suppress("SameParameterValue")
@Service
class SkadeforklaringService(
    private val pdfService: PdfService,
    private val pdlClient: PdlClient,
    private val dokarkivClient: DokarkivClient,
    private val bigQueryClient: BigQueryClient
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()


    fun mottaSkadeforklaring(record: SkadeforklaringInnsendingHendelse) {
        log.info("Mottatt ny skadeforklaring")
        secureLogger.info("Mottatt ny skadeforklaring: $record")
        val pdf = pdfService.lagPdf(record, PdfTemplate.SKADEFORKLARING_TRO_KOPI)
        val beriketData = lagBeriketData(record)
        val beriketPdf = lagBeriketPdf(record, beriketData, PdfTemplate.SKADEFORKLARING_BERIKET)
        val opprettJournalpostRequest = mapSkadeforklaringTilOpprettJournalpostRequest(record, beriketData, pdf, beriketPdf)
        dokarkivClient.journalfoerDokument(opprettJournalpostRequest)
//        foerMetrikkIBigQuery(record)
    }


    private fun lagBeriketData(record: SkadeforklaringInnsendingHendelse): BeriketData {
        val innmeldersFnr = record.skadeforklaring.innmelder?.norskIdentitetsnummer
        val skadelidtsFnr = record.skadeforklaring.skadelidt?.norskIdentitetsnummer
        val innmeldersNavn: Navn? = hentNavnFraPersondataloesningen(innmeldersFnr)
        val skadelidtsNavn: Navn? = hentNavnFraPersondataloesningen(skadelidtsFnr)
        return BeriketData(innmeldersNavn, skadelidtsNavn, null)
    }

    private fun lagBeriketPdf(
        record: SkadeforklaringInnsendingHendelse,
        beriketData: BeriketData,
        pdfTemplateBeriket: PdfTemplate
    ): ByteArray {
        return pdfService.lagBeriketPdf(record, beriketData, pdfTemplateBeriket)
    }

    private fun hentNavnFraPersondataloesningen(fodselsnummer: String?): Navn? {
        if (fodselsnummer == null) {
            return null
        }
        return pdlClient.hentNavn(fodselsnummer)
    }

    private fun mapSkadeforklaringTilOpprettJournalpostRequest(
        record: SkadeforklaringInnsendingHendelse,
        beriketData: BeriketData,
        pdf: ByteArray,
        beriketPdf: ByteArray
    ): OpprettJournalpostRequest {
        val skadeforklaring = record.skadeforklaring
        val skadeforklaringJson = objectMapper.writeValueAsString(skadeforklaring)

        return OpprettJournalpostRequest(
            tittel = DIGITAL_SKADEFORKLARING_TITTEL,
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                navn = beriketData.innmeldersNavn.toString(),
                id = skadeforklaring.innmelder?.norskIdentitetsnummer ?: "",
                idType = BrukerIdType.FNR
            ),
            bruker = Bruker(
                id = skadeforklaring.skadelidt?.norskIdentitetsnummer,
                idType = BrukerIdType.FNR
            ),
            tema = TEMA_YRKESSKADE,
            kanal = Kanal.NAV_NO.toString(),
            datoMottatt = record.metadata.tidspunktMottatt.toString(),
            eksternReferanseId = record.metadata.navCallId,
            dokumenter = listOf(
                Dokument(
                    brevkode = DIGITAL_SKADEFORKLARING_BREVKODE,
                    tittel = DIGITAL_SKADEFORKLARING_TITTEL,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.PDFA,
                            variantformat = Dokumentvariantformat.ARKIV,
                            fysiskDokument = beriketPdf
                        )
                    )
                ),
                Dokument(
                    brevkode = DIGITAL_SKADEFORKLARING_BREVKODE,
                    tittel = DIGITAL_SKADEFORKLARING_ARKIV_TITTEL,
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
            )
        )
    }

}