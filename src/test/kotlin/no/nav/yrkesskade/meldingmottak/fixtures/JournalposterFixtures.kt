package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalposter.Journalpost
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun journalposter(): List<Journalpost?> =
    listOf(journalpostMedSak(), journalpostUtenSak(), journalpostUtenSakEldreEnn24Mnd())

fun forGamleJournalposter(): List<Journalpost?> =
    listOf(journalpostUtenSakEldreEnn24Mnd())

fun journalposterMedSak(): List<Journalpost?> =
    listOf(journalpostMedSak())

fun journalpostMedSak(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(2).truncatedTo(ChronoUnit.DAYS),
        sak = com.expediagroup.graphql.generated.journalposter.Sak(
            sakstype = Sakstype.FAGSAK,
            tema = Tema.YRK
        )
    )

fun journalpostUtenSak(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(4).truncatedTo(ChronoUnit.DAYS),
        sak = null
    )

fun journalpostUtenSakEldreEnn24Mnd(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(24).minusDays(1).truncatedTo(ChronoUnit.DAYS),
        sak = null
    )
