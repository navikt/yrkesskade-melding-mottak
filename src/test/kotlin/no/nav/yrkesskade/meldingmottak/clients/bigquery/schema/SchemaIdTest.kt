package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.google.cloud.bigquery.DatasetId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SchemaIdTest {

    @Test
    internal fun `should create valid SchemaId from String`() {
        val schemaId = SchemaId.of("foobar_v1")

        assertThat(schemaId.name).isEqualTo("foobar")
        assertThat(schemaId.version).isEqualTo(1)
    }

    @Test
    internal fun `should create valid TableId from SchemaId`() {
        val tableName = "foobar_v1"
        val datasetId = DatasetId.of("foo", "bar")
        val schemaId = SchemaId.of(tableName)
        val tableId = schemaId.toTableId(datasetId)

        assertThat(tableId.project).isEqualTo(datasetId.project)
        assertThat(tableId.dataset).isEqualTo(datasetId.dataset)
        assertThat(tableId.table).isEqualTo(tableName)
    }
}
