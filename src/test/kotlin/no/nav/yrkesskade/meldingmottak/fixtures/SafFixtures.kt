package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLError
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLResponse
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLSourceLocation
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.Journalposter
import com.expediagroup.graphql.generated.Saker
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalposter.Dokumentoversikt
import com.expediagroup.graphql.generated.saker.Sak
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun errorJournalpostRespons(): GraphQLClientResponse<Journalpost.Result> =
    JacksonGraphQLResponse(
        data = null,
        errors = listOf(
            JacksonGraphQLError(
                message = "Validation error",
                locations = listOf(JacksonGraphQLSourceLocation(line = 1, column = 1))
            )
        ),
        extensions = emptyMap()
    )

fun okJournalpostRespons(): GraphQLClientResponse<Journalpost.Result> =
    JacksonGraphQLResponse(
        data = Journalpost.Result(gyldigJournalpostMedAktoerId()),
        errors = null,
        extensions = emptyMap()
    )

fun errorJournalposterRespons(): GraphQLClientResponse<Journalposter.Result> =
    JacksonGraphQLResponse(
        data = null,
        errors = listOf(
            JacksonGraphQLError(
                message = "Validation error",
                locations = listOf(JacksonGraphQLSourceLocation(line = 1, column = 1))
            )
        ),
        extensions = emptyMap()
    )

fun okJournalposterRespons(): GraphQLClientResponse<Journalposter.Result> =
    JacksonGraphQLResponse(
        data = Journalposter.Result(Dokumentoversikt(journalposter())),
        errors = null,
        extensions = emptyMap()
    )

fun errorSakerRespons(): GraphQLClientResponse<Saker.Result> =
    JacksonGraphQLResponse(
        data = null,
        errors = listOf(
            JacksonGraphQLError(
                message = "Validation error",
                locations = listOf(JacksonGraphQLSourceLocation(line = 1, column = 1))
            )
        ),
        extensions = emptyMap()
    )

fun okSakerResponse(): GraphQLClientResponse<Saker.Result> =
    JacksonGraphQLResponse(
        data = sakerResultMedGenerellYrkesskadesak(),
        errors = null,
        extensions = null
    )

fun sakerResult(): Saker.Result =
    Saker.Result(saker())

fun sakerResultMedGenerellYrkesskadesak(): Saker.Result =
    Saker.Result(sakerHvoravGenerellYrkesskadesak())

fun sakerResultMedForGammelGenerellYrkesskadesak(): Saker.Result =
    Saker.Result(sakerMedForGammelGenerellYrkesskadesak())

fun journalposterResult(): Journalposter.Result =
    Journalposter.Result(Dokumentoversikt(journalposter()))

fun forGamleJournalposterResult(): Journalposter.Result =
    Journalposter.Result(Dokumentoversikt(forGamleJournalposter()))

fun journalposterResultMedSak(): Journalposter.Result =
    Journalposter.Result(Dokumentoversikt(journalposterMedSak()))

fun saker(): List<Sak> =
    listOf(fagsakAnnetTema())

fun sakerHvoravGenerellYrkesskadesak(): List<Sak> =
    listOf(generellYrkesskadesak(), fagsakAnnetTema(), generellYrkesskadesakEldreEnn24Mnd())

fun sakerMedForGammelGenerellYrkesskadesak(): List<Sak> =
    listOf(fagsakAnnetTema(), generellYrkesskadesakEldreEnn24Mnd())

fun generellYrkesskadesak(): Sak =
    Sak(
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(2).truncatedTo(ChronoUnit.DAYS),
        fagsakId = "62",
        fagsaksystem = "FS22",
        sakstype = Sakstype.GENERELL_SAK,
        tema = Tema.YRK
    )

fun generellYrkesskadesakEldreEnn24Mnd(): Sak =
    Sak(
        datoOpprettet = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(24).minusDays(1).truncatedTo(ChronoUnit.DAYS),
        fagsakId = "61",
        fagsaksystem = "FS22",
        sakstype = Sakstype.GENERELL_SAK,
        tema = Tema.YRK
    )

fun fagsakAnnetTema(): Sak =
    Sak(
        datoOpprettet = LocalDateTime.of(2022, Month.APRIL, 13, 13, 0, 0),
        fagsakId = "123",
        fagsaksystem = "A001",
        sakstype = Sakstype.FAGSAK,
        tema = Tema.BAR
    )
