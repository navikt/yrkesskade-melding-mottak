package no.nav.yrkesskade.skadeforklaring.v1.handler

import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.*
import no.nav.yrkesskade.meldingmottak.services.PdfService
import no.nav.yrkesskade.meldingmottak.services.PdfTemplate
import no.nav.yrkesskade.meldingmottak.services.StorageService
import no.nav.yrkesskade.meldingmottak.vedlegg.AttachmentTypeUnsupportedException
import no.nav.yrkesskade.meldingmottak.vedlegg.Image2PDFConverter
import no.nav.yrkesskade.meldingmottak.vedlegg.VedleggUtil
import no.nav.yrkesskade.skadeforklaring.integration.mottak.AbstractSkadeforklaringMottakHandler
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.v1.model.Vedleggreferanse
import no.nav.yrkesskade.storage.Blob

class MottakHandler(
    pdfService: PdfService,
    pdlClient: PdlClient,
    storageService: StorageService,
    image2PDFConverter: Image2PDFConverter
) : AbstractSkadeforklaringMottakHandler<SkadeforklaringInnsendingHendelse>(
    pdfService,
    pdlClient,
    storageService,
    image2PDFConverter
) {
    override fun motta(record: SkadeforklaringInnsendingHendelse): OpprettJournalpostRequest {
        log.info("Mottatt ny skadeforklaring")
        secureLogger.info("Mottatt ny skadeforklaring: $record")
        val pdf = pdfService.lagPdf(record, PdfTemplate.SKADEFORKLARING_TRO_KOPI)
        val beriketData = lagBeriketData(record)
        val beriketPdf = lagBeriketPdf(record, beriketData, PdfTemplate.SKADEFORKLARING_BERIKET)
        val vedleggdokumenter = opprettDokumenter(
            record.skadeforklaring.vedleggreferanser,
            record.skadeforklaring.innmelder?.norskIdentitetsnummer ?: ""
        )
        return mapSkadeforklaringTilOpprettJournalpostRequest(record.skadeforklaring, record.metadata.tidspunktMottatt, record.metadata.navCallId, beriketData, pdf, beriketPdf, vedleggdokumenter)
    }

    private fun lagBeriketData(record: SkadeforklaringInnsendingHendelse): BeriketData {
        val innmeldersFnr = record.skadeforklaring.innmelder?.norskIdentitetsnummer
        val skadelidtsFnr = record.skadeforklaring.skadelidt?.norskIdentitetsnummer
        val innmeldersNavn: Navn? = hentNavnFraPersondataloesningen(innmeldersFnr)
        val skadelidtsNavn: Navn? = hentNavnFraPersondataloesningen(skadelidtsFnr)
        return BeriketData(innmeldersNavn, skadelidtsNavn, innmeldersFnr, skadelidtsFnr, null)
    }

    private fun lagBeriketPdf(
        record: SkadeforklaringInnsendingHendelse,
        beriketData: BeriketData,
        pdfTemplateBeriket: PdfTemplate
    ): ByteArray {
        return pdfService.lagBeriketPdf(record, beriketData, pdfTemplateBeriket)
    }

    internal fun opprettDokumenter(
        vedleggreferanser: List<Vedleggreferanse>,
        dokumentEierIdentifikator: String
    ): List<Dokument> {
        return vedleggreferanser.mapNotNull { vedleggreferanse ->
            opprettDokument(vedleggreferanse, dokumentEierIdentifikator)
        }.toList()
    }

    private fun opprettDokument(vedleggreferanse: Vedleggreferanse, dokumentEierIdentifikator: String): Dokument? {
        val vedlegg: Blob =
            storageService.hent(vedleggreferanse.id, dokumentEierIdentifikator) ?: opprettBlobForManglendeVedlegg(
                vedleggreferanse.navn,
                dokumentEierIdentifikator
            )
        var bytes = vedlegg.bytes
        var filtype: Filtype? = VedleggUtil.utledFiltype(bytes, vedlegg.navn)
            ?: throw AttachmentTypeUnsupportedException(
                "Kan ikke utlede filtypen for vedlegg ${vedlegg.navn}",
                VedleggUtil.mediaType(bytes),
                null
            )

        if (!VedleggUtil.gyldigVedleggFiltype(filtype)) {
            throw AttachmentTypeUnsupportedException(
                "Kan ikke journalføre vedlegg av typen $filtype, ${vedlegg.navn}",
                VedleggUtil.mediaType(bytes),
                null
            )
        }

        if (bytes != null && bytes.isNotEmpty() && VedleggUtil.gyldigBildevedleggFiltype(filtype)) {
            bytes = image2PDFConverter.convert(bytes)
            filtype = Filtype.PDF
        }

        return Dokument(
            brevkode = "",
            tittel = vedlegg.navn ?: "Vedlegg",
            dokumentvarianter = listOf(
                Dokumentvariant(
                    filtype = filtype!!,
                    variantformat = Dokumentvariantformat.ARKIV,
                    fysiskDokument = bytes ?: byteArrayOf()
                )
            )
        )
    }
}