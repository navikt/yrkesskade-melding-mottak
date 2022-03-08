package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.PdfClient
import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.pdf.PdfSkademeldingMapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfSkademelding
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.stereotype.Service

@Service
class PdfService(
    private val pdfClient: PdfClient
) {

    fun lagPdf(record: SkademeldingInnsendtHendelse, template: PdfTemplate): ByteArray {
        val pdfSkademelding: PdfSkademelding = PdfSkademeldingMapper.tilPdfSkademelding(record)
        return pdfClient.lagPdf(pdfSkademelding, template)
    }

    fun lagBeriketPdf(record: SkademeldingInnsendtHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val pdfSkademelding: PdfSkademelding = PdfSkademeldingMapper.tilPdfSkademelding(record, beriketData)
        return pdfClient.lagPdf(pdfSkademelding, template)
    }

}

enum class PdfTemplate(val templatenavn: String) {
    SKADEMELDING_TRO_KOPI("skademelding-tro-kopi"),
    SKADEMELDING_SAKSBEHANDLING("skademelding-saksbehandling")
}