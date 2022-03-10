package no.nav.yrkesskade.meldingmottak.clients.bigquery

import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.DatasetId
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import com.google.cloud.bigquery.TableInfo
import no.nav.yrkesskade.meldingmottak.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles

@Component
class BigQueryClientProvider(
    @Value("\${spring.cloud.gcp.bigquery.project-id}") val bigQueryProjectId: String,
    @Value("\${spring.cloud.gcp.bigquery.dataset-name}") val bigQueryDatasetName: String
    ) {

    @Bean
    @ConditionalOnProperty(
        value = ["spring.cloud.gcp.bigquery.enabled"],
        havingValue = "true"
    )
    fun defaultBigQueryClient(): BigQueryClient {
        return DefaultBigQueryClient(DatasetId.of(bigQueryProjectId, bigQueryDatasetName))
    }

    @Bean
    @ConditionalOnProperty(
        value = ["spring.cloud.gcp.bigquery.enabled"],
        havingValue = "false"
    )
    fun bigQueryClientStub(): BigQueryClient {
        return BigQueryClientStub()
    }
}

interface BigQueryClient {
    fun tablePresent(tableId: TableId): Boolean
    fun create(tableInfo: TableInfo): TableInfo
    fun insert(tableId: TableId, row: InsertAllRequest.RowToInsert)
    fun migrate()

    class BigQueryClientException(message: String) : RuntimeException(message)
}

class DefaultBigQueryClient(private val datasetId: DatasetId) : BigQueryClient {
    val logger = getLogger(MethodHandles.lookup().lookupClass())

    private val bigQuery = BigQueryOptions.newBuilder()
        .setProjectId(datasetId.project)
        .build()
        .service

    init {
        migrate()
    }

    override fun tablePresent(tableId: TableId): Boolean {
        return (bigQuery.getTable(tableId) != null).also { present ->
            logger.info("table: $tableId, present: $present")
        }
    }

    override fun create(tableInfo: TableInfo): TableInfo {
        return bigQuery.create(tableInfo).also { createdTable ->
            logger.info ("Opprettet tabell: '${createdTable.tableId.table}'")
        }
    }

    override fun insert(tableId: TableId, row: InsertAllRequest.RowToInsert) {
        val tableName = tableId.table
        val table = requireNotNull(bigQuery.getTable(tableId)) {
            "Mangler tabell: '$tableName' i BigQuery"
        }
        val rows = listOf(row)
        logger.debug("Setter inn rader i tabell: '$tableName', rader: '$rows'")
        val response = table.insert(rows)
        if (response.hasErrors()) {
            throw BigQueryClient.BigQueryClientException(
                "Lagring i BigQuery feilet: '${response.insertErrors}'"
            )
        }
        logger.info("Rader ble lagret i tabell: '$tableName'")
    }

    override fun migrate() {
        logger.info("Migrerer BigQuery")
        schemaRegistry
            .mapValues { it.value.toTableInfo(datasetId) }
            .filterValues { !tablePresent(it.tableId) }
            .forEach { (_, tableInfo) ->
                bigQuery.create(tableInfo)
            }
        logger.info("Migrert BigQuery")
    }
}

class BigQueryClientStub : BigQueryClient {
    val log = getLogger(MethodHandles.lookup().lookupClass())

    override fun tablePresent(tableId: TableId): Boolean {
        log.info ("tablePresent(tableId) called with tableId: '$tableId'")
        return true
    }

    override fun create(tableInfo: TableInfo): TableInfo {
        log.info("create(tableInfo) called with tableInfo: '$tableInfo'")
        return tableInfo
    }

    override fun insert(tableId: TableId, row: InsertAllRequest.RowToInsert) {
        log.info("insert(tableId, row) called with tableId: '$tableId', row: '$row'")
    }

    override fun migrate() {
        log.info("migrate() called")
    }
}