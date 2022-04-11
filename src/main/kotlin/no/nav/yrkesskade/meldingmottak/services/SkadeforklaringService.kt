package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.Adresse
import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

@Service
class SkadeforklaringService(
    private val pdfService: PdfService,
    private val pdlClient: PdlClient
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()


    // mottak av skadeforklaring er ikke ferdig implementert ennå.
    // vil skadeforklaring komme på en egen kafka topic?
    fun mottaSkadeforklaring(record: SkadeforklaringInnsendingHendelse) {
        log.info("Mottatt ny skadeforklaring")
        secureLogger.info("Mottatt ny skadeforklaring: $record")
        val pdf = pdfService.lagPdf(record, PdfTemplate.SKADEFORKLARING_TRO_KOPI)
        val beriketPdf = lagBeriketPdf(record, PdfTemplate.SKADEFORKLARING_BERIKET)
//        val opprettJournalpostRequest = mapSkadeforklaringTilOpprettJournalpostRequest(record, pdf, beriketPdf)
//        dokarkivClient.journalfoerSkadeforklaring(opprettJournalpostRequest)
//        foerMetrikkIBigQuery(record)
    }


    private fun lagBeriketPdf(record: SkadeforklaringInnsendingHendelse, pdfTemplateBeriket: PdfTemplate): ByteArray {
        val innmeldersFnr = record.skadeforklaring.innmelder.norskIdentitetsnummer
        val skadelidtsFnr = record.skadeforklaring.skadelidt.norskIdentitetsnummer
        val beriketData = lagBeriketData(innmeldersFnr, skadelidtsFnr)
        return pdfService.lagBeriketPdf(record, beriketData, pdfTemplateBeriket)
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

}