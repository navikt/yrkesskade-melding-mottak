package no.nav.yrkesskade.meldingmottak.fixtures

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
        .setMottaksKanal("SKAN_IM")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalfoeringHendelseRecordMedTemaSYK(): JournalfoeringHendelseRecord {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("journalpostStatus")
        .setTemaGammelt("YRK")
        .setTemaNytt("SYK")
        .setMottaksKanal("NRK")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalfoeringHendelseRecordMedKanalNAVNO(): JournalfoeringHendelseRecord {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("journalpostStatus")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("NAV_NO")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}
