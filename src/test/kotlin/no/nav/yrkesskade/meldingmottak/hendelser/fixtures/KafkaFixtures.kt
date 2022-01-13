package no.nav.yrkesskade.meldingmottak.hendelser.fixtures

import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

fun journalfoeringHendelseRecord(): JournalfoeringHendelseRecord? {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("journalpostStatus")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("NRK")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalpostResultWithBrukerAktoerid(): Journalpost.Result {
    return Journalpost.Result(
        com.expediagroup.graphql.generated.journalpost.Journalpost(
            journalpostId = "1337",
            journalstatus = Journalstatus.MOTTATT,
            tema = Tema.YRK,
            bruker = Bruker("2751737180290", BrukerIdType.AKTOERID),
            dokumenter = listOf(
                DokumentInfo(
                    "Melding om yrkesskade eller yrkessykdom som er påført under tjeneste på skip eller under fiske/fangst",
                    "NAV 13-07.08"
                )
            )
        )
    )
}

fun journalpostResultWithBrukerFnr(): Journalpost.Result {
    return Journalpost.Result(
        com.expediagroup.graphql.generated.journalpost.Journalpost(
            journalpostId = "1337",
            journalstatus = Journalstatus.MOTTATT,
            tema = Tema.YRK,
            bruker = Bruker("12345678901", BrukerIdType.FNR),
            dokumenter = listOf(
                DokumentInfo(
                    "Melding om yrkesskade eller yrkessykdom som er påført under tjeneste på skip eller under fiske/fangst",
                    "NAV 13-07.08"
                )
            )
        )
    )
}
