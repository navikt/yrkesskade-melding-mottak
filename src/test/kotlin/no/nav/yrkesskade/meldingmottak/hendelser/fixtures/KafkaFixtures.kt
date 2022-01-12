package no.nav.yrkesskade.meldingmottak.hendelser.fixtures

import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
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
                        journalpostId = "123",
                        journalstatus = Journalstatus.MOTTATT,
                        tema = Tema.YRK,
                        bruker = Bruker("2751737180290", BrukerIdType.AKTOERID),
                        dokumenter = emptyList()
                )
        )
}

fun journalpostResultWithBrukerFnr(): Journalpost.Result {
        return Journalpost.Result(
                com.expediagroup.graphql.generated.journalpost.Journalpost(
                        journalpostId = "123",
                        journalstatus = Journalstatus.MOTTATT,
                        tema = Tema.YRK,
                        bruker = Bruker("12345678901", BrukerIdType.FNR),
                        dokumenter = emptyList()
                )
        )
}
