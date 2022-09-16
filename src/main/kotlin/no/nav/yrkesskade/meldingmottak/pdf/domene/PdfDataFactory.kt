package no.nav.yrkesskade.meldingmottak.pdf.domene

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring.PdfSkadeforklaringMapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.skadeforklaring.PdfSkadeforklaringV2Mapper
import no.nav.yrkesskade.meldingmottak.pdf.domene.skademelding.PdfSkademeldingMapper
import no.nav.yrkesskade.meldingmottak.util.kodeverk.KodeverkHolder
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import java.lang.IllegalStateException
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV2
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV1


class PdfDataFactory {

    companion object {
        fun tilPdfData(record: Any, kodeverkHolder: KodeverkHolder, beriketData: BeriketData? = null) =
            when (record) {
                is SkadeforklaringInnsendingHendelseV2 -> PdfSkadeforklaringV2Mapper.tilPdfSkadeforklaring(record, kodeverkHolder, beriketData)
                is SkadeforklaringInnsendingHendelseV1 -> PdfSkadeforklaringMapper.tilPdfSkadeforklaring(record, kodeverkHolder, beriketData)
                is SkademeldingInnsendtHendelse -> PdfSkademeldingMapper.tilPdfSkademelding(record, kodeverkHolder, beriketData)
                else -> throw IllegalStateException("${record.javaClass.name} er ikke en kjent PDF data klasse")
            }
    }
}