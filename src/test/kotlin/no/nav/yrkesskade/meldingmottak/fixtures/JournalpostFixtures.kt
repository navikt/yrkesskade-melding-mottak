package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import java.time.LocalDateTime

fun gyldigJournalpostMedAktoerId(): com.expediagroup.graphql.generated.journalpost.Journalpost {
    return com.expediagroup.graphql.generated.journalpost.Journalpost(
        journalpostId = "1337",
        journalstatus = Journalstatus.MOTTATT,
        journalposttype = Journalposttype.I,
        tema = Tema.YRK,
        bruker = Bruker("2751737180290", BrukerIdType.AKTOERID),
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
            bruker = Bruker("12345678901", BrukerIdType.FNR),
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