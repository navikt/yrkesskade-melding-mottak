package no.nav.yrkesskade.meldingmottak.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClient
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.SkademeldingPayload
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.skademelding_v1
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.*
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.transaction.Transactional

private const val TEMA_YRKESSKADE = "YRK"

private const val DIGITAL_SKADEMELDING_ARKIV_TITTEL = "Arkivlogg fra innsending"

private const val DIGITAL_SKADEMELDING_TITTEL = "Melding om yrkesskade eller yrkessykdom"

private const val DIGITAL_SKADEMELDING_BREVKODE = "NAV 13"

@Service
class SkademeldingService(
    private val pdfService: PdfService,
    private val pdlClient: PdlClient,
    private val dokarkivClient: DokarkivClient,
    private val bigQueryClient: BigQueryClient
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()

    /**
     * lag PDF
     * journalfør i dokarkiv
     * registrer metrikker
     */
    @Transactional
    fun mottaSkademelding(record: SkademeldingInnsendtHendelse) {
        log.info("Mottatt ny skademelding")
        secureLogger.info("Mottatt ny skademelding: $record")
        val pdf = pdfService.lagPdf(record, PdfTemplate.SKADEMELDING_TRO_KOPI)
        val beriketPdf = lagBeriketPdf(record, PdfTemplate.SKADEMELDING_SAKSBEHANDLING)
        val opprettJournalpostRequest = mapSkademeldingTilOpprettJournalpostRequest(record, pdf, beriketPdf)
        dokarkivClient.journalfoerDokument(opprettJournalpostRequest)?.also {
            foerMetrikkIBigQuery(record)
        }
    }


    @Suppress("SameParameterValue")
    private fun lagBeriketPdf(record: SkademeldingInnsendtHendelse, pdfTemplateSaksbehandling: PdfTemplate): ByteArray {
        val innmeldersFnr = record.skademelding.innmelder?.norskIdentitetsnummer
        val skadelidtsFnr = record.skademelding.skadelidt?.norskIdentitetsnummer
        val beriketData = lagBeriketData(innmeldersFnr, skadelidtsFnr)
        return pdfService.lagBeriketPdf(record, beriketData, pdfTemplateSaksbehandling)
    }

    private fun lagBeriketData(
        innmeldersFnr: String?,
        skadelidtsFnr: String?
    ): BeriketData {
        val innmeldersNavn: Navn? = hentNavnFraPersondataloesningen(innmeldersFnr)
        val skadelidtsNavnOgAdresse: Pair<Navn?, Adresse?> = hentNavnOgAdresse(skadelidtsFnr)
        return BeriketData(innmeldersNavn, skadelidtsNavnOgAdresse.first, skadelidtsNavnOgAdresse.second)
    }

    private fun hentNavnFraPersondataloesningen(fodselsnummer: String?): Navn? {
        if (fodselsnummer == null) {
            return null
        }
        return pdlClient.hentNavn(fodselsnummer)
    }

    private fun hentNavnOgAdresse(fodselsnummer: String?): Pair<Navn?, Adresse?> {
        if (fodselsnummer == null) {
            return Pair(null, null)
        }
        return pdlClient.hentNavnOgAdresse(fodselsnummer)
    }

    private fun mapSkademeldingTilOpprettJournalpostRequest(
        record: SkademeldingInnsendtHendelse,
        pdf: ByteArray,
        beriketPdf: ByteArray
    ): OpprettJournalpostRequest {
        val skademelding = record.skademelding
        val skademeldingJson = objectMapper.writeValueAsString(skademelding)

        return OpprettJournalpostRequest(
            tittel = DIGITAL_SKADEMELDING_TITTEL,
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                navn = record.beriketData.innmeldersOrganisasjonsnavn.first,
                id = skademelding.innmelder?.paaVegneAv.orEmpty(),
                idType = BrukerIdType.ORGNR
            ),
            bruker = Bruker(
                id = skademelding.skadelidt!!.norskIdentitetsnummer,
                idType = BrukerIdType.FNR
            ),
            tema = TEMA_YRKESSKADE,
            kanal = Kanal.NAV_NO.toString(),
            datoMottatt = record.metadata.tidspunktMottatt.toString(),
            eksternReferanseId = record.metadata.navCallId,
            dokumenter = listOf(
                Dokument(
                    brevkode = DIGITAL_SKADEMELDING_BREVKODE,
                    tittel = DIGITAL_SKADEMELDING_TITTEL,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.PDFA,
                            variantformat = Dokumentvariantformat.ARKIV,
                            fysiskDokument = beriketPdf
                        )
                    )
                ),
                Dokument(
                    brevkode = DIGITAL_SKADEMELDING_BREVKODE,
                    tittel = DIGITAL_SKADEMELDING_ARKIV_TITTEL,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.JSON,
                            variantformat = Dokumentvariantformat.ORIGINAL,
                            fysiskDokument = skademeldingJson.encodeToByteArray()
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

    private fun foerMetrikkIBigQuery(record: SkademeldingInnsendtHendelse) {
        val skademeldingPayload = SkademeldingPayload(
            kilde = record.metadata.kilde,
            tidspunktMottatt = record.metadata.tidspunktMottatt,
            spraak = record.metadata.spraak.toString(),
            callId = record.metadata.navCallId,
            rolletype = record.skademelding.skadelidt.dekningsforhold.rolletype,
            tidstype = record.skademelding.hendelsesfakta.tid.tidstype.value
        )
        bigQueryClient.insert(
            skademelding_v1,
            skademelding_v1.transform(objectMapper.valueToTree(skademeldingPayload))
        )
    }

}
