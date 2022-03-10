package no.nav.yrkesskade.meldingmottak.clients.bigquery

import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse

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

    override fun transform(payload: SkademeldingInnsendtHendelse): InsertAllRequest.RowToInsert =
        InsertAllRequest.RowToInsert.of(
            mapOf(
                "kilde" to payload.metadata.kilde,
                "tidspunktMottatt" to payload.metadata.tidspunktMottatt.toString(),
                "spraak" to payload.metadata.spraak,
                "callId" to payload.metadata.navCallId,
                "opprettet" to "AUTO"
            )
        )
}