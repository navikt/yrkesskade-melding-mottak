package no.nav.yrkesskade.skadeforklaring.v2.handler

import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.services.PdfService
import no.nav.yrkesskade.meldingmottak.services.StorageService
import no.nav.yrkesskade.meldingmottak.vedlegg.Image2PDFConverter
import no.nav.yrkesskade.skadeforklaring.integration.mottak.AbstractSkadeforklaringMottakHandler
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse

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
        TODO("Not yet implemented")
    }
}