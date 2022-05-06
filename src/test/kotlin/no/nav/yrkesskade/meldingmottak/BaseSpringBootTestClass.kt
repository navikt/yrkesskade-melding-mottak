package no.nav.yrkesskade.meldingmottak

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils

/**
 * Liten abstrakt klasse for det som skal være felles for våre tester som trenger en hel
 * Spring Boot-kontekst.
 * Gir oss en MockOAuth2Server og en Postgres.
 */
@ActiveProfiles("test")
@SpringBootTest
@EnableMockOAuth2Server
@ContextConfiguration(initializers = [BaseSpringBootTestClass.DockerConfigInitializer::class])
abstract class BaseSpringBootTestClass {
    init {
        PostgresDockerContainer.container
    }

    class DockerConfigInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.datasource.url=" + PostgresDockerContainer.container.jdbcUrl,
                "spring.datasource.username=" + PostgresDockerContainer.container.username,
                "spring.datasource.password=" + PostgresDockerContainer.container.password,
            );
        }
    }
}