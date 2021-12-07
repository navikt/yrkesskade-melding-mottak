package no.nav.yrkesskade.meldingmottak.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class OppgaveClientConfiguration(private val webClientBuilder: WebClient.Builder,
                                 @Value("\${oppgave.url}") val oppgaveServiceURL: String) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Bean
    fun oppgaveWebClient(): WebClient {
        return webClientBuilder
            .baseUrl(oppgaveServiceURL)
            .clientConnector(ReactorClientHttpConnector(HttpClient.newConnection()))
            .build()
    }
}