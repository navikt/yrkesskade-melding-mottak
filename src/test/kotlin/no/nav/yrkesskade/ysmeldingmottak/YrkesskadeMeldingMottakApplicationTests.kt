package no.nav.yrkesskade.ysmeldingmottak

import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

//@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class YrkesskadeMeldingMottakApplicationTests {
	@Container
	var postgreSQLContainer: PostgreSQLContainer<*> = IntegrationTestPostgresqlContainer.getInstance()
	@Test
	fun contextLoads() {
	}

}
