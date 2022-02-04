package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLResponse
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.HentIdenter

fun okResponsFraPdl(): GraphQLClientResponse<HentIdenter.Result> {
    return JacksonGraphQLResponse(
        data = HentIdenter.Result(gyldigIdentlisteMedAktorId()),
        errors = null,
        extensions = emptyMap()
    )
}