package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema

val journalfoeringhendelse_oppgave_v1 = object : SchemaDefinition {
    override val schemaId: SchemaId = SchemaId(name = "journalfoeringhendelse_oppgave", version = 1)

    override fun define(): Schema = schema {
        string("journalpostId") {
            required()
            description("Systemet som sendte skademeldingen")
        }
        string("tittel") {
            required()
            description("Tidspunkt da skademeldingen ble mottatt")
        }
        string("kanal") {
            required()
            description("Skademeldingens språk")
        }
        string("brevkode") {
            required()
            description("Unik ID for innmeldingens systemtransaksjon")
        }
        string("tildeltEnhetsnr") {
            required()
            description("Unik ID for innmeldingens systemtransaksjon")
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
        val journalfoeringHendelseOppgavePayload =
            jacksonObjectMapper().treeToValue<JournalfoeringHendelseOppgavePayload>(payload)

        return InsertAllRequest.RowToInsert.of(
            mapOf(
                "journalpostId" to journalfoeringHendelseOppgavePayload.journalpostId,
                "tittel" to journalfoeringHendelseOppgavePayload.tittel,
                "kanal" to journalfoeringHendelseOppgavePayload.kanal,
                "brevkode" to journalfoeringHendelseOppgavePayload.brevkode,
                "enhetFraJournalpost" to journalfoeringHendelseOppgavePayload.enhetFraJournalpost,
                "tildeltEnhetsnr" to journalfoeringHendelseOppgavePayload.tildeltEnhetsnr,
                "callId" to journalfoeringHendelseOppgavePayload.callId,
                "opprettet" to "AUTO"
            )
        )
    }
}

data class JournalfoeringHendelseOppgavePayload(
    val journalpostId: String,
    val tittel: String,
    val kanal: String,
    val brevkode: String,
    val enhetFraJournalpost: String,
    val tildeltEnhetsnr: String,
    val callId: String
)