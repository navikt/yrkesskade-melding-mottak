package no.nav.yrkesskade.meldingmottak.domene

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
    val eksternReferanseId: String,
    val datoMottatt: String,
    val dokumenter: List<Dokument>
)

data class AvsenderMottaker(
    val navn: String,
    val id: String,
    val idType: BrukerIdType,
)

data class Bruker(
    val id: String?,
    val idType: BrukerIdType?
)

enum class BrukerIdType {
    AKTOERID,
    FNR,
    ORGNR
}

enum class Journalposttype {
    INNGAAENDE, UTGAAENDE, NOTAT
}

enum class Filtype {
    PDF,
    PDFA,
    JSON,
    XML,
    JPEG,
    PNG,
    RTF
}

enum class Dokumentvariantformat {
    ORIGINAL,
    ARKIV
}

enum class Kanal {
    SKAN_NETS, SKAN_IM, NAV_NO
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