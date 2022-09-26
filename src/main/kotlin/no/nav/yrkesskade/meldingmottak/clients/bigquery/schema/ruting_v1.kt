package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema

val ruting_v1 = object : SchemaDefinition {
    override val schemaId: SchemaId = SchemaId(name = "ruting", version = 1)
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun define(): Schema = schema {
        string("brevkode") {
            required()
            description("brevkode")
        }
        string("journalpostId") {
            required()
            description("journalpostId")
        }
        string("tilSystem") {
            required()
            description("Systemet innmeldingen rutes til")
        }
        string("rutingAarsak") {
            nullable()
            description("Årsak til rutingen")
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
        val rutingPayload = objectMapper.treeToValue<JournalfoeringHendelseRutingPayload>(payload)
        return InsertAllRequest.RowToInsert.of(
            mapOf(
                "brevkode" to rutingPayload.brevkode,
                "journalpostId" to rutingPayload.journalpostId,
                "tilSystem" to rutingPayload.tilSystem,
                "rutingAarsak" to rutingPayload.rutingAarsak,
                "callId" to rutingPayload.callId,
                "opprettet" to "AUTO"
            )
        )
    }
}


data class JournalfoeringHendelseRutingPayload(
    val brevkode: String,
    val journalpostId: String,
    val tilSystem: String,
    val rutingAarsak: String?,
    val callId: String
)
