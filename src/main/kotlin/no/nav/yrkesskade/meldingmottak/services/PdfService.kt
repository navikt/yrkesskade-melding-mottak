package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.PdfClient
import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfData
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfDataFactory
import no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring.PdfSkadeforklaringMapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.PdfSkademeldingMapper
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV1
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV2
import org.springframework.stereotype.Service

@Service
class PdfService(
    private val pdfClient: PdfClient,
    private val kodeverkservice: KodeverkService
) {

    fun lagPdf(record: SkademeldingInnsendtHendelse, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(record.skademelding.skadelidt.dekningsforhold.rolletype, kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkademeldingInnsendtHendelse, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(record.skademelding.skadelidt.dekningsforhold.rolletype, kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagPdf(record: SkadeforklaringInnsendingHendelseV1, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkadeforklaringInnsendingHendelseV1, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagPdf(record: SkadeforklaringInnsendingHendelseV2, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder)
        return pdfClient.lagPdf(pdfData, template)
    }

    fun lagBeriketPdf(record: SkadeforklaringInnsendingHendelseV2, beriketData: BeriketData?, template: PdfTemplate): ByteArray {
        val kodeverkHolder = KodeverkHolder.init(kodeverkService = kodeverkservice)
        val pdfData: PdfData = PdfDataFactory.tilPdfData(record, kodeverkHolder, beriketData)
        return pdfClient.lagPdf(pdfData, template)
    }
}

enum class PdfTemplate(val templatenavn: String) {
    SKADEMELDING_TRO_KOPI("skademelding-tro-kopi"),
    SKADEMELDING_SAKSBEHANDLING("skademelding-saksbehandling"),
    SKADEFORKLARING_TRO_KOPI("skadeforklaring-tro-kopi"),
    SKADEFORKLARING_BERIKET("skadeforklaring-beriket"),
    SKADEFORKLARING_V2_TRO_KOPI("skadeforklaring-v2-tro-kopi"),
    SKADEFORKLARING_V2_BERIKET("skadeforklaring-v2-beriket")
}