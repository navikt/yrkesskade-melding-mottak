package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalposter.DokumentInfo
import com.expediagroup.graphql.generated.journalposter.Journalpost
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun journalposter(): List<Journalpost?> =
    listOf(journalpostMedSak(), journalpostUtenSak(), journalpostUtenSakEldreEnn24Mnd())

fun journalposterMedTannlegeerklaeringUtenSak(): List<Journalpost?> =
    listOf(journalpostTannlegeerklaeringUtenSak())

fun forGamleJournalposter(): List<Journalpost?> =
    listOf(journalpostUtenSakEldreEnn24Mnd())

fun journalposterMedSak(): List<Journalpost?> =
    listOf(journalpostMedSak())

fun journalposterUtenSakOgDokumenter(): List<Journalpost?> =
    listOf(journalpostUtenSakOgDokumenter())

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
        ),
        dokumenter = listOf(
            DokumentInfo("1", "Melding om yrkesskade eller yrkessykdom", "NAV 13")
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
        sak = null,
        dokumenter = listOf(
            DokumentInfo("1", "Melding om yrkesskade eller yrkessykdom", "NAV 13")
        )
    )

fun journalpostUtenSakOgDokumenter(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(4).truncatedTo(ChronoUnit.DAYS),
        sak = null,
        dokumenter = null
    )

fun journalpostTannlegeerklaeringUtenSak(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(4).truncatedTo(ChronoUnit.DAYS),
        sak = null,
        dokumenter = listOf(
            DokumentInfo("2", "Tannlegeerklæring ved yrkesskade", Brevkode.TANNLEGEERKLAERING.kode)
        )
    )

fun journalpostUtenSakEldreEnn24Mnd(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(24).minusDays(1).truncatedTo(ChronoUnit.DAYS),
        sak = null,
        dokumenter = listOf(
            DokumentInfo("1", "Melding om yrkesskade eller yrkessykdom", "NAV 13")
        )
    )
