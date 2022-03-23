package no.nav.yrkesskade.meldingmottak.fixtures

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.model.Systemkilde
import no.nav.yrkesskade.skademelding.model.Skademelding
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID

fun journalfoeringHendelseRecord(): JournalfoeringHendelseRecord? {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("MOTTATT")
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
        .setJournalpostStatus("MOTTATT")
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
        .setJournalpostStatus("MOTTATT")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("NAV_NO")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalfoeringHendelseRecordMedKanalALTINN(): JournalfoeringHendelseRecord {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("MOTTATT")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("ALTINN")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalfoeringHendelseRecordMedJournalpoststatusJOURNALFOERT(): JournalfoeringHendelseRecord {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("JOURNALFOERT")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("ALTINN")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun journalfoeringHendelseRecordMedKanalSKAN_NETS(): JournalfoeringHendelseRecord {
    return JournalfoeringHendelseRecord.newBuilder()
        .setHendelsesId("hendelsesId")
        .setVersjon(1)
        .setHendelsesType("hendelsesType")
        .setJournalpostId(1337)
        .setJournalpostStatus("MOTTATT")
        .setTemaGammelt("YRK")
        .setTemaNytt("YRK")
        .setMottaksKanal("SKAN_NETS")
        .setKanalReferanseId("P1")
        .setBehandlingstema("YRK")
        .build()
}

fun skademeldingInnsendtHendelse(): SkademeldingInnsendtHendelse {
    val skademelding: Skademelding = jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(
        Files.readString(Path.of("src/test/resources/skademeldinger/digitalSkademelding.json"))
    )
    return SkademeldingInnsendtHendelse(
        skademelding = skademelding,
        metadata = SkademeldingMetadata(
            kilde = "Webskjema",
            tidspunktMottatt = Instant.now(),
            spraak = Spraak.NB,
            navCallId = UUID.randomUUID().toString()
        ),
        beriketData = SkademeldingBeriketData(
            innmeldersOrganisasjonsnavn = "NAV IT" to Systemkilde.ENHETSREGISTERET
        )
    )
}