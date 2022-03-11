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
            description("journalpostId")
        }
        string("tittel") {
            required()
            description("Tittelen på hoveddokumentet i journalposten")
        }
        string("kanal") {
            required()
            description("Innmeldingskanalen")
        }
        string("brevkode") {
            required()
            description("brevkode")
        }
        string("behandlingstema") {
            required()
            description("Behandlingstema fra journalposten")
        }
        string("enhetFraJournalpost") {
            required()
            description("Overstyrende enhet fra journalposten")
        }
        string("tildeltEnhetsnr") {
            required()
            description("Enheten som oppgaven faktisk ble rutet til")
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
                "behandlingstema" to journalfoeringHendelseOppgavePayload.behandlingstema,
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
    val behandlingstema: String,
    val enhetFraJournalpost: String,
    val tildeltEnhetsnr: String,
    val callId: String
)