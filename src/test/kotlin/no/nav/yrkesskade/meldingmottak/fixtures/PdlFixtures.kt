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
        data = HentPerson.Result(gyldigPersonMedNavnOgVegadresse()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsPersonMedMatrikkeladresseFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigPersonMedNavnOgMatrikkeladresse()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsPersonMedUkjentBostedFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigPersonMedUkjentBosted()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsPersonMedEnkelUtenlandskAdresseFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigPersonMedEnkelUtenlandskAdresse()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsPersonMedUtenlandskAdresseFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigPersonMedUtenlandskAdresse()),
        errors = null,
        extensions = emptyMap()
    )
}

/**
 * Response med fortrolig person (kode 7)
 */
fun okResponsFortroligPersonFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigFortroligPersonMedNavnOgVegadresse()),
        errors = null,
        extensions = emptyMap()
    )
}

/**
 * Response med strengt fortrolig person (kode 6)
 */
fun okResponsStrengtFortroligPersonFraPdl(): GraphQLClientResponse<HentPerson.Result> {
    return JacksonGraphQLResponse(
        data = HentPerson.Result(gyldigStrengtFortroligPersonMedNavnOgVegadresse()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsAdresseFraPdl(): GraphQLClientResponse<HentAdresse.Result> {
    return JacksonGraphQLResponse(
        data = HentAdresse.Result(gyldigVegadresse()),
        errors = null,
        extensions = emptyMap()
    )
}

fun okResponsMatrikkeladresseFraPdl(): GraphQLClientResponse<HentAdresse.Result> {
    return JacksonGraphQLResponse(
        data = HentAdresse.Result(gyldigMatrikkeladresse()),
        errors = null,
        extensions = emptyMap()
    )

}
