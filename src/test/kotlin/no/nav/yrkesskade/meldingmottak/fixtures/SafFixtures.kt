package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLError
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLResponse
import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLSourceLocation
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost

fun errorRespons(): GraphQLClientResponse<Journalpost.Result> {
    return JacksonGraphQLResponse(
        data = null,
        errors = listOf(
            JacksonGraphQLError(
                message = "Validation error",
                locations = listOf(JacksonGraphQLSourceLocation(line = 1, column = 1))
            )
        ),
        extensions = emptyMap()
    )
}

fun okRespons(): GraphQLClientResponse<Journalpost.Result> {
    return JacksonGraphQLResponse(
        data = Journalpost.Result(gyldigJournalpostMedAktoerId()),
        errors = null,
        extensions = emptyMap()
    )
}