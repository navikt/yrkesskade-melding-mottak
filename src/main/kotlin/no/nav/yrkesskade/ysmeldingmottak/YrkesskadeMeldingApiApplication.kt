package no.nav.yrkesskade.ysmeldingmottak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class YrkesskadeMeldingApiApplication

fun main(args: Array<String>) {
	runApplication<YrkesskadeMeldingApiApplication>(*args)
}
