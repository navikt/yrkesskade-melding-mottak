package no.nav.yrkesskade.ysmeldingmottak.repository

import no.nav.yrkesskade.ysmeldingmottak.domain.Skademelding
import no.nav.yrkesskade.ysmeldingmottak.repositories.SkademeldingRepository
import no.nav.yrkesskade.ysmeldingmottak.testutils.docker.PostgresDockerContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Profile
import java.time.Instant

@DataJpaTest
@Profile("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkademeldingRepositoryTest {

    @Autowired
    lateinit var repository: SkademeldingRepository

    init {
        PostgresDockerContainer.container
    }

    @Test
    fun contextLoads() {
        repository.save(
                Skademelding(
                        null,
                        """{"some": "data"}""",
                        "me",
                        Instant.now()
                )
        )
        assertThat(repository.findAll().size).isEqualTo(1)
    }
}
