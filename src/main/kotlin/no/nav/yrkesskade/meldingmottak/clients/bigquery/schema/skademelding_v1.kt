package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema
import java.time.Instant

val skademelding_v1 = object : SchemaDefinition {
    override val schemaId: SchemaId = SchemaId(name = "skademelding", version = 1)
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun define(): Schema = schema {
        string("kilde") {
            required()
            description("Systemet som sendte skademeldingen")
        }
        timestamp("tidspunktMottatt") {
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
        val skademeldingPayload = objectMapper.treeToValue<SkademeldingPayload>(payload)
        return InsertAllRequest.RowToInsert.of(
            mapOf(
                "kilde" to skademeldingPayload.kilde,
                "tidspunktMottatt" to skademeldingPayload.tidspunktMottatt.toString(),
                "spraak" to skademeldingPayload.spraak,
                "callId" to skademeldingPayload.callId,
                "opprettet" to "AUTO"
            )
        )
    }
}

data class SkademeldingPayload(
    val kilde: String,
    val tidspunktMottatt: Instant,
    val spraak: String,
    val callId: String
)
