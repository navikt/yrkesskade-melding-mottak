package no.nav.yrkesskade.meldingmottak.services

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.DatasetId
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.FieldList
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName
import com.google.cloud.bigquery.StandardTableDefinition
import com.google.cloud.bigquery.TableId
import com.google.cloud.bigquery.TableInfo
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import javax.annotation.PostConstruct


@Service
class BigQueryService(
    private val bigQuery: BigQuery,
    @Value("\${spring.cloud.gcp.bigquery.dataset-name}") val bigQueryDatasetName: String,
    @Value("\${spring.cloud.gcp.bigquery.project-id}") val bigQueryProjectId: String
) {

    private val logger = getLogger(MethodHandles.lookup().lookupClass())

    fun tablePresent(tableId: TableId): Boolean = bigQuery.getTable(tableId) != null
    fun getDatasetId(): DatasetId = bigQuery.getDataset("$bigQueryDatasetName:$bigQueryProjectId").datasetId

    @PostConstruct
    fun migrate() {
        logger.info("Migrerer BigQuery")
        schemaRegistry
            .mapValues { it.value.toTableInfo(getDatasetId()) }
            .filterValues { !tablePresent(it.tableId) }
            .forEach { (_, tableInfo) ->
                bigQuery.create(tableInfo)
            }
        logger.info("Migrert BigQuery")
    }
}

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

class FieldBuilder(private val name: String, private val type: StandardSQLTypeName) {
    private var mode: Field.Mode = Field.Mode.NULLABLE
    private var description: String? = null
    private var subFields: FieldList? = null

    fun nullable() {
        this.mode = Field.Mode.NULLABLE
    }

    fun required() {
        this.mode = Field.Mode.REQUIRED
    }

    fun repeated() {
        this.mode = Field.Mode.REPEATED
    }

    fun description(description: String) {
        this.description = description
    }

    fun subFields(block: SchemaBuilder.() -> Unit) {
        this.subFields = SchemaBuilder().apply(block).fieldList()
    }

    fun build(): Field = Field.newBuilder(name, type, subFields)
        .setMode(mode)
        .setDescription(description)
        .build()
}

class SchemaBuilder {
    private val fields = mutableListOf<Field>()

    fun fieldList(): FieldList = FieldList.of(fields)

    fun build(): Schema = Schema.of(fields)

    private fun field(
        name: String,
        type: StandardSQLTypeName,
        block: FieldBuilder.() -> Unit = {},
    ): Field = FieldBuilder(name, type)
        .apply(block)
        .build()
        .also { fields.add(it) }

    fun datetime(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.DATETIME, block)

    fun string(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.STRING, block)

    fun struct(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.STRUCT, block)

    fun timestamp(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.TIMESTAMP, block)
}

fun schema(block: SchemaBuilder.() -> Unit): Schema = SchemaBuilder()
    .apply(block)
    .build()


interface SchemaDefinition {
    val schemaId: SchemaId

    fun entry() = schemaId to this

    fun define(): Schema

    fun transform(payload: SkademeldingInnsendtHendelse): InsertAllRequest.RowToInsert

    fun toTableInfo(datasetId: DatasetId): TableInfo {
        val tableDefinition = StandardTableDefinition.of(define())
        return TableInfo.of(schemaId.toTableId(datasetId), tableDefinition)
    }
}

data class SchemaId(val name: String, val version: Int) {

    fun toTableId(datasetId: DatasetId): TableId = TableId.of(
        datasetId.project,
        datasetId.dataset,
        listOf(name, version).joinToString(SEPARATOR),
    )

    companion object {
        private const val SEPARATOR = "_v"

        fun of(value: String) = value.split(SEPARATOR).let {
            SchemaId(it.first(), it.last().toInt())
        }
    }
}

val schemaRegistry: Map<SchemaId, SchemaDefinition> = mapOf(
    skademelding_v1.entry(),
)

