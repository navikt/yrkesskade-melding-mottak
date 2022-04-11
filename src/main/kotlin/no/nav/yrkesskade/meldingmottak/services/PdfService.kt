package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.PdfClient
import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring.PdfSkadeforklaringMapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.PdfSkademeldingMapper
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.stereotype.Service

@Service
class PdfService(
    private val pdfClient: PdfClient,
    private val kodeverkservice: KodeverkService
) {

    fun lagPdf(record: SkademeldingInnsendtHendelse, template: PdfTemplate): ByteArray {
        val pdfData: PdfData = PdfSkademeldingMapper.tilPdfSkademelding(record, landkoder())
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkademeldingInnsendtHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val pdfData: PdfData = PdfSkademeldingMapper.tilPdfSkademelding(record, landkoder(), beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagPdf(record: SkadeforklaringInnsendingHendelse, template: PdfTemplate): ByteArray {
        val pdfData: PdfData = PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkadeforklaringInnsendingHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val pdfData: PdfData = PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }

    private fun landkoder(spraak: String = "nb"): Map<KodeverkKode, KodeverkVerdi> {
        return kodeverkservice.hentKodeverk("landkoder", "", spraak)
    }
}

enum class PdfTemplate(val templatenavn: String) {
    SKADEMELDING_TRO_KOPI("skademelding-tro-kopi"),
    SKADEMELDING_SAKSBEHANDLING("skademelding-saksbehandling"),
    SKADEFORKLARING_TRO_KOPI("skadeforklaring-tro-kopi"),
    SKADEFORKLARING_BERIKET("skadeforklaring-beriket")
}