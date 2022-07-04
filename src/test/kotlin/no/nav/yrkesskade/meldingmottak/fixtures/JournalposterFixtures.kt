package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalposter.Journalpost

fun journalposter(): List<Journalpost?> =
    listOf(journalpostMedSak(), journalpostUtenSak())

fun journalposterMedSak(): List<Journalpost?> =
    listOf(journalpostMedSak())

fun journalpostMedSak(): Journalpost =
    Journalpost(
        journalpostId = "71",
        tittel = "Melding om yrkesskade eller yrkessykdom",
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        tema = Tema.YRK,
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
        sak = null
    )
