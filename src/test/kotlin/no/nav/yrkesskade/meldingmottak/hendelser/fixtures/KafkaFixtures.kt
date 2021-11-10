package no.nav.yrkesskade.meldingmottak.hendelser.fixtures

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