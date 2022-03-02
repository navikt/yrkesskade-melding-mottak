package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.AvsenderMottaker
import no.nav.yrkesskade.meldingmottak.domene.Bruker
import no.nav.yrkesskade.meldingmottak.domene.BrukerIdType
import no.nav.yrkesskade.meldingmottak.domene.Dokument
import no.nav.yrkesskade.meldingmottak.domene.Dokumentvariant
import no.nav.yrkesskade.meldingmottak.domene.Dokumentvariantformat
import no.nav.yrkesskade.meldingmottak.domene.Filtype
import no.nav.yrkesskade.meldingmottak.domene.Journalposttype
import no.nav.yrkesskade.meldingmottak.domene.Kanal
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import java.time.Instant
import java.util.UUID

fun opprettJournalpostOkRespons() =
    OpprettJournalpostResponse(
        journalpostferdigstilt = false,
        journalpostId = "1234",
        dokumenter = emptyList()
    )

fun opprettJournalpostRequest() = OpprettJournalpostRequest(
    tittel = "skademelding",
    journalposttype = Journalposttype.INNGAAENDE,
    avsenderMottaker = AvsenderMottaker(
        id = "12345699999",
        idType = BrukerIdType.FNR
    ),
    bruker = Bruker(
        id = "12345699999",
        type = BrukerIdType.FNR
    ),
    tema = "YRK",
    kanal = Kanal.NAV_NO.toString(),
    datoMottatt = Instant.now().toString(),
    eksternReferanseId = UUID.randomUUID().toString(),
    dokumenter = listOf(
        Dokument(
            brevkode = "NAV 13",
            tittel = "skademelding",
            dokumentvarianter = listOf(
                Dokumentvariant(
                    filtype = Filtype.JSON,
                    variantformat = Dokumentvariantformat.ORIGINAL,
                    fysiskDokument = "test".encodeToByteArray()
                )
            )
        )
    )
)
