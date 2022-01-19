package no.nav.yrkesskade.meldingmottak.repository

import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.sql.Connection
import java.sql.DriverManager

/**
 * Test at databasetabeller er opprettet for Task. Mer utførlige db-tester for task, finnes i kode-repository
 * yrkesskade-prosessering-backend, hvor task repositorys finnes.
 *
 * @DataJpaTest er benyttet for å få lastet db-migreringene på enklest måte (drar opp Spring)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskDatabaseIT {

    lateinit var connection: Connection

    init {
        PostgresDockerContainer.container
    }

    @BeforeEach
    fun setUp() {
        connection = DriverManager.getConnection(Companion.JDBC_URL, "test", "test")
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

    @AfterEach
    fun tearDown() {
        connection.close()
    }


    companion object {
        const val JDBC_URL = "jdbc:postgresql://localhost:5432/test"
    }
}