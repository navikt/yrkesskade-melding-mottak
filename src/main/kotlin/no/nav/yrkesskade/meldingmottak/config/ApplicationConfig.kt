package no.nav.yrkesskade.meldingmottak.config

import no.nav.yrkesskade.meldingmottak.config.FeatureToggleConfig.Companion.ERIK_TESTER
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles


@SpringBootConfiguration
@ComponentScan("no.nav.yrkesskade.prosessering", "no.nav.yrkesskade.meldingmottak", "no.nav.familie.sikkerhet")
@EntityScan("no.nav.yrkesskade.prosessering", "no.nav.yrkesskade.meldingmottak")
@EnableRetry
@ConfigurationPropertiesScan
@EnableScheduling
class ApplicationConfig

@Component
class TemporaryScheduledTask(private val featureToggleService: FeatureToggleService) {

    @Scheduled(fixedRate = 1000)
    fun scheduleFixedRateTaskAsync() {
        if (featureToggleService.isEnabled(ERIK_TESTER)) {
            logger.info("Feature $ERIK_TESTER er enabled")
        } else {
            logger.info("Feature $ERIK_TESTER er disabled")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}