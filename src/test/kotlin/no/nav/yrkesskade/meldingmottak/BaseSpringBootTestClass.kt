package no.nav.yrkesskade.meldingmottak

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.yrkesskade.meldingmottak.testutils.docker.PostgresDockerContainer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Liten abstrakt klasse for det som skal være felles for våre tester som trenger en hel
 * Spring Boot-kontekst.
 * Gir oss en MockOAuth2Server og en Postgres.
 */
@ActiveProfiles("test")
@SpringBootTest
@EnableMockOAuth2Server
abstract class BaseSpringBootTestClass {
    init {
        PostgresDockerContainer.container
    }
}