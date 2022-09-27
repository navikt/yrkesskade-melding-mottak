package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.PdfClient
import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring.PdfSkadeforklaringMapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.PdfSkademeldingMapper
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.springframework.stereotype.Service

@Service
class PdfService(
    private val pdfClient: PdfClient,
    private val kodeverkservice: KodeverkService
) {

    fun lagPdf(record: SkademeldingInnsendtHendelse, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(record.skademelding.skadelidt.dekningsforhold.rolletype, kodeverkservice)
        val pdfData: PdfData = PdfSkademeldingMapper.tilPdfSkademelding(record, kodeverkHolder)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkademeldingInnsendtHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(record.skademelding.skadelidt.dekningsforhold.rolletype, kodeverkservice)
        val pdfData: PdfData = PdfSkademeldingMapper.tilPdfSkademelding(record, kodeverkHolder, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagPdf(record: SkadeforklaringInnsendingHendelse, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record, kodeverkHolder)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkadeforklaringInnsendingHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record, kodeverkHolder, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }
}

enum class PdfTemplate(val templatenavn: String) {
    SKADEMELDING_TRO_KOPI("skademelding-tro-kopi"),
    SKADEMELDING_SAKSBEHANDLING("skademelding-saksbehandling"),
    SKADEFORKLARING_TRO_KOPI("skadeforklaring-tro-kopi"),
    SKADEFORKLARING_BERIKET("skadeforklaring-beriket")
}