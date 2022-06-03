package no.nav.yrkesskade.meldingmottak.repository

import no.nav.yrkesskade.meldingmottak.BaseSpringBootTestClass
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Test at databasetabeller er opprettet for Task. Mer utførlige db-tester for task, finnes i kode-repository
 * yrkesskade-prosessering-backend, hvor task repositorys finnes.
 *
 * @DataJpaTest er benyttet for å få lastet db-migreringene på enklest måte (drar opp Spring)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [BaseSpringBootTestClass.DockerConfigInitializer::class])
class TaskDatabaseIT {

    init {
        PostgresDockerContainer.container
    }

    lateinit var connection: Connection

    @Value("\${spring.datasource.url}")
    lateinit var jdbcUrl: String

    @Value("\${spring.datasource.username}")
    lateinit var username: String

    @Value("\${spring.datasource.password}")
    lateinit var password: String

    @BeforeEach
    fun setUp() {
        connection = DriverManager.getConnection(jdbcUrl, username, password)
        resetDatabase()
    }

    @Transactional
    fun resetDatabase() {
        connection.prepareStatement("TRUNCATE TABLE task CASCADE").execute()
    }

    @Test
    fun `skal kunne lagre en task og hente den opp igjen`() {
        assertThat(connection.isValid(0)).isTrue

        val select = connection.prepareStatement("select * from task")
        val emptyResultSet = select.executeQuery()
        assertThat(emptyResultSet.next()).isFalse

        val insert = connection.prepareStatement(
            "insert into task " +
                    "(id, payload, status, versjon, opprettet_tid, type, metadata, trigger_tid, avvikstype) " +
                    "values(101, 'blabla', 'FIN-FIN', 1, '2022-01-19', 'TYPE1', 'noen metadata', '2022-01-19', 'ANNET')"
        )
        println("Insert task: $insert")
        insert.executeUpdate()

        val resultSet = select.executeQuery()
        assertThat(resultSet.next()).isTrue
        val taskId = resultSet.getLong("id")
        assertThat(taskId).isEqualTo(101)

        // Test task_logg tabell:
        val selectLogg = connection.prepareStatement("select * from task_logg")
        val ingenLoggerRS = selectLogg.executeQuery()
        assertThat(ingenLoggerRS.next()).isFalse

        val insertLogg = connection.prepareStatement(
            "insert into task_logg " +
                    "(id, task_id, type, node, opprettet_tid, melding, endret_av) " +
                    "values(1012, $taskId, 'TYPE2', 'Node 2', '2022-01-19', 'Dette er en melding', 'Utvikler')"
        )
        println("Insert task_logg: $insertLogg")
        insertLogg.executeUpdate()

        val loggerResult = select.executeQuery()
        assertThat(loggerResult.next()).isTrue
    }

    @Test
    fun `skal ikke kunne lagre to tasker med identisk payload`() {
        assertThat(connection.isValid(0)).isTrue

        val select = connection.prepareStatement("select * from task")
        val emptyResultSet = select.executeQuery()
        assertThat(emptyResultSet.next()).isFalse

        val insert = connection.prepareStatement(
            "insert into task " +
                    "(id, payload, payload_hash, status, versjon, opprettet_tid, type, metadata, trigger_tid, avvikstype) " +
                    "values(101, 'blabla', 'en fin hashverdi', 'FIN-FIN', 1, '2022-01-19', 'TYPE1', 'noen metadata', '2022-01-19', 'ANNET')"
        )
        insert.executeUpdate()

        val insertForDuplisertTask = connection.prepareStatement(
            "insert into task " +
                    "(id, payload, payload_hash, status, versjon, opprettet_tid, type, metadata, trigger_tid, avvikstype) " +
                    "values(102, 'blabla', 'en fin hashverdi', 'FIN-FIN', 1, '2022-01-19', 'TYPE1', 'noen metadata', '2022-01-19', 'ANNET')"
        )
        val thrown = assertThrows<SQLException> { insertForDuplisertTask.executeUpdate() }
        assertThat(thrown.message).contains("""duplicate key value violates unique constraint "payload_hash_key"""")
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }
}