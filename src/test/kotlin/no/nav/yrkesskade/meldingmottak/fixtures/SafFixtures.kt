package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLError
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLResponse
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLSourceLocation
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.Saker
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.saker.Sak
import java.time.LocalDateTime
import java.time.Month

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

fun sakerResultMedGenerellYrkesskadesak(): Saker.Result =
    Saker.Result(sakerHvoravGenerellYrkesskadesak())

fun sakerHvoravGenerellYrkesskadesak(): List<Sak> =
    listOf(generellYrkesskadesak(), fagsakAnnetTema())

fun generellYrkesskadesak(): Sak =
    Sak(
        datoOpprettet = LocalDateTime.of(2022, Month.MARCH, 8, 11, 0, 0),
        fagsakId = null,
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
