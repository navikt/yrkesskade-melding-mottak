package no.nav.yrkesskade.meldingmottak.testutils.docker

import org.testcontainers.containers.PostgreSQLContainer

class PostgresDockerContainer private constructor() : PostgreSQLContainer<PostgresDockerContainer>(IMAGE_NAME) {
    companion object {
        const val IMAGE_NAME = "postgres:12"
        val container: PostgresDockerContainer by lazy {
            PostgresDockerContainer().apply {
                this.start()
            }
        }
    }
}