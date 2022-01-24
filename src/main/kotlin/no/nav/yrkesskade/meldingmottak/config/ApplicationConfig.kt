package no.nav.yrkesskade.meldingmottak.config

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@EnableJpaAuditing
@EnableJdbcRepositories("no.nav.yrkesskade"/*, "no.nav.familie"*/)
@ComponentScan("no.nav.yrkesskade.prosessering", "no.nav.yrkesskade.meldingmottak", "no.nav.familie.sikkerhet")
@EntityScan("no.nav.yrkesskade.prosessering", "no.nav.yrkesskade.meldingmottak")
@EnableRetry
@ConfigurationPropertiesScan
@EnableScheduling
class ApplicationConfig