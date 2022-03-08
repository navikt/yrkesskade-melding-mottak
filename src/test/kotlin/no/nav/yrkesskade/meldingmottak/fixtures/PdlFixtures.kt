package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLResponse
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.HentAdresse
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.HentPerson

fun okResponsIdenterFraPdl(): GraphQLClientResponse<HentIdenter.Result> {
    return JacksonGraphQLResponse(
        data = HentIdenter.Result(gyldigIdentlisteMedAktorId()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsPersonFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigPersonMedNavn()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsAdresseFraPdl(): GraphQLClientResponse<HentAdresse.Result> {
    return JacksonGraphQLResponse(
        data = HentAdresse.Result(gyldigAdresseMedVeg()),
        errors = null,
        extensions = emptyMap()
    )
}
