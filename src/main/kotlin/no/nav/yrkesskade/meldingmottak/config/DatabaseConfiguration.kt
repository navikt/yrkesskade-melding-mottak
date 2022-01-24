package no.nav.yrkesskade.meldingmottak.config

import no.nav.yrkesskade.prosessering.PropertiesWrapperTilStringConverter
import no.nav.yrkesskade.prosessering.StringTilPropertiesWrapperConverter
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories("no.nav.yrkesskade")
@EnableJpaRepositories(
    "no.nav.yrkesskade",
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = arrayOf(TaskRepository::class))]
)
@EnableJpaAuditing
class DatabaseConfiguration : AbstractJdbcConfiguration() {

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            listOf(
                StringTilPropertiesWrapperConverter(),
                PropertiesWrapperTilStringConverter()
            )
        )
    }
}