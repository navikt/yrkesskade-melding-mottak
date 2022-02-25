package no.nav.yrkesskade.meldingmottak.hendelser.domene

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.journalpost.Bruker
import java.util.Date

enum class Journalpoststatus {
    MOTTATT
}

data class OpprettJournalpostResponse(
    val journalpostferdigstilt: Boolean,
    val journalpostId: String,
    val dokumenter: List<DokumentInfoId>
)

data class DokumentInfoId(
    val dokumentInfoId: String
)

data class OpprettJournalpostRequest(
//    val forsoekFerdigstill: Boolean,
    val tittel: String,
    val journalposttype: Journalposttype,
    val avsenderMottaker: AvsenderMottaker,
    val bruker: Bruker,
    val tema: String? = "YRK",
    val kanal: String? = "NAV_NO",
//    val eksternReferanseId: String -- her kan vi evt legge vår egen skademeldingsId.
    val datoMottatt: Date,
    val dokumenter: List<Dokument>
)

data class AvsenderMottaker(
    val id: String,
    val idType: BrukerIdType,
)

enum class Journalposttype {
    INNGAAENDE, UTGAAENDE, NOTAT
}

enum class Filtype {
    PDFA,
    JSON
}

enum class Dokumentvariantformat {
    ORIGINAL,
    ARKIV
}

data class Dokument(
    val brevkode: String?,
    val tittel: String?,
    val dokumentvarianter: List<Dokumentvariant>
)

data class Dokumentvariant(
    val filtype: Filtype,
    val variantformat: Dokumentvariantformat,
    val fysiskDokument: ByteArray
)