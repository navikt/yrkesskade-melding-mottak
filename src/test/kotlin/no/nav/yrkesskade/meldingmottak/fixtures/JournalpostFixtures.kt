package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Kanal
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import java.time.LocalDateTime

fun gyldigJournalpostMedAktoerId(): com.expediagroup.graphql.generated.journalpost.Journalpost {
    return com.expediagroup.graphql.generated.journalpost.Journalpost(
        journalpostId = "1337",
        journalstatus = Journalstatus.MOTTATT,
        journalposttype = Journalposttype.I,
        tema = Tema.YRK,
        kanal = Kanal.SKAN_IM,
        bruker = Bruker("2751737180290", BrukerIdType.AKTOERID),
        journalfoerendeEnhet = "4849",
        behandlingstema = null,
        dokumenter = listOf(
            DokumentInfo(
                "Melding om yrkesskade eller yrkessykdom som er påført under tjeneste på skip eller under fiske/fangst",
                "NAV 13-07.08"
            )
        ),
        datoOpprettet = LocalDateTime.of(2022, 1, 1, 1, 1, 1, 1)
    )
}

fun journalpostResultWithBrukerAktoerid(): Journalpost.Result {
    return Journalpost.Result(gyldigJournalpostMedAktoerId())
}

fun journalpostResultWithBrukerFnr(): Journalpost.Result {
    return Journalpost.Result(
        com.expediagroup.graphql.generated.journalpost.Journalpost(
            journalpostId = "1337",
            journalstatus = Journalstatus.MOTTATT,
            journalposttype = Journalposttype.I,
            tema = Tema.YRK,
            kanal = Kanal.SKAN_IM,
            bruker = Bruker("12345678901", BrukerIdType.FNR),
            journalfoerendeEnhet = "4849",
            behandlingstema = null,
            dokumenter = listOf(
                DokumentInfo(
                    "Melding om yrkesskade eller yrkessykdom som er påført under tjeneste på skip eller under fiske/fangst",
                    "NAV 13-07.08"
                )
            ),
            datoOpprettet = LocalDateTime.of(2022, 1, 1, 1, 1, 1, 1)
        )
    )
}

fun journalpostResultTannlegeerklaering(): Journalpost.Result {
    val journalpostMedJournalstatusFeilregistrert = gyldigJournalpostMedAktoerId().copy(
        dokumenter = listOf(
            DokumentInfo(
                "Tannlegeerklæring ved yrkesskade",
                Brevkode.TANNLEGEERKLAERING.kode
            )
        )
    )
    return Journalpost.Result(journalpostMedJournalstatusFeilregistrert)
}

fun journalpostResultMedJournalstatusFeilregistrert(): Journalpost.Result {
    val journalpostMedJournalstatusFeilregistrert = gyldigJournalpostMedAktoerId().copy(
        journalstatus = Journalstatus.FEILREGISTRERT
    )
    return Journalpost.Result(journalpostMedJournalstatusFeilregistrert)
}

fun journalpostResultMedTemaSYK(): Journalpost.Result {
    val journalpostMedTemaSyk = gyldigJournalpostMedAktoerId().copy(
        tema = Tema.SYK
    )
    return Journalpost.Result(journalpostMedTemaSyk)
}

fun journalpostResultUtenDokumenter(): Journalpost.Result {
    val journalpostUtenDokumenter = gyldigJournalpostMedAktoerId().copy(
        dokumenter = null
    )
    return Journalpost.Result(journalpostUtenDokumenter)
}

fun journalpostResultUtenBrukerId(): Journalpost.Result {
    val journalpostUtenBrukerId = gyldigJournalpostMedAktoerId().copy(
        bruker = Bruker(null, BrukerIdType.AKTOERID)
    )
    return Journalpost.Result(journalpostUtenBrukerId)
}

fun journalpostResultUtenBruker(): Journalpost.Result {
    val journalpostUtenBruker = gyldigJournalpostMedAktoerId().copy(
        bruker = null
    )
    return Journalpost.Result(journalpostUtenBruker)
}

fun journalpostResultMedUgyldigBrukerIdType(): Journalpost.Result {
    val journalpostMedUgyldigBrukerIdType = gyldigJournalpostMedAktoerId().copy(
        bruker = Bruker("920165931", BrukerIdType.ORGNR)
    )
    return Journalpost.Result(journalpostMedUgyldigBrukerIdType)
}

fun journalpostResultMedJournalposttypeUtgaaende(): Journalpost.Result {
    val journalpostMedJournalposttypeUtgaaende = gyldigJournalpostMedAktoerId().copy(
        journalposttype = Journalposttype.U
    )
    return Journalpost.Result(journalpostMedJournalposttypeUtgaaende)
}
