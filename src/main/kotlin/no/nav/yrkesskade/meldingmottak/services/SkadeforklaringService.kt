package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.vedlegg.Image2PDFConverter
import no.nav.yrkesskade.skadeforklaring.integration.mottak.ISkadeforklaringInnsendingHendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import no.nav.yrkesskade.skadeforklaring.v1.handler.MottakHandler as MottakV1Handler
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV1
import no.nav.yrkesskade.skadeforklaring.v2.handler.MottakHandler as MottakV2Handler
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV2


@Suppress("SameParameterValue")
@Service
class SkadeforklaringService(
    private val pdfService: PdfService,
    private val pdlClient: PdlClient,
    private val dokarkivClient: DokarkivClient,
    private val storageService: StorageService,
    private val image2PDFConverter: Image2PDFConverter
) {

    @Transactional
    fun mottaSkadeforklaring(record: ISkadeforklaringInnsendingHendelse) {
        val opprettJournalpostRequest = when (record) {
            is SkadeforklaringInnsendingHendelseV1 -> MottakV1Handler(pdfService, pdlClient, storageService, image2PDFConverter).motta(record)
            is SkadeforklaringInnsendingHendelseV2 -> MottakV2Handler(pdfService, pdlClient, storageService, image2PDFConverter).motta(record)
            else -> throw IllegalStateException("record er ikke en kjent SkadeforklaringInnsendtHendelse")
        }
        dokarkivClient.journalfoerDokument(opprettJournalpostRequest)
    }

}