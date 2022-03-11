package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema

val skademelding_v1 = object : SchemaDefinition {
    override val schemaId: SchemaId = SchemaId(name = "skademelding", version = 1)

    override fun define(): Schema = schema {
        string("kilde") {
            required()
            description("Systemet som sendte skademeldingen")
        }
        string("tidspunktMottatt") {
            required()
            description("Tidspunkt da skademeldingen ble mottatt")
        }
        string("spraak") {
            required()
            description("Skademeldingens språk")
        }
        string("callId") {
            required()
            description("Unik ID for innmeldingens systemtransaksjon")
        }
        timestamp("opprettet") {
            required()
            description("Tidsstempel for lagring av hendelsen")
        }
    }

    override fun transform(payload: JsonNode): InsertAllRequest.RowToInsert {
        val skademeldingPayload = jacksonObjectMapper().treeToValue<SkademeldingPayload>(payload)
        return InsertAllRequest.RowToInsert.of(
            mapOf(
                "kilde" to skademeldingPayload.kilde,
                "tidspunktMottatt" to skademeldingPayload.tidspunktMottatt,
                "spraak" to skademeldingPayload.spraak,
                "callId" to skademeldingPayload.callId,
                "opprettet" to "AUTO"
            )
        )
    }
}

data class SkademeldingPayload(
    val kilde: String,
    val tidspunktMottatt: String,
    val spraak: String,
    val callId: String
)
