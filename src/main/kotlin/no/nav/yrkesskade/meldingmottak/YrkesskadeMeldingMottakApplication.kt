package no.nav.yrkesskade.meldingmottak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class YrkesskadeMeldingApiApplication

fun main(args: Array<String>) {
	runApplication<YrkesskadeMeldingApiApplication>(*args)
}
