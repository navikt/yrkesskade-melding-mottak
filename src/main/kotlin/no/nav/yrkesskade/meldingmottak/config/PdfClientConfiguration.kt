package no.nav.yrkesskade.meldingmottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class PdfClientConfiguration(private val webClientBuilder: WebClient.Builder,
                             @Value("\${YRKESSKADE_DOKGEN_API_URL}") val pdfServiceURL: String) {

    @Bean
    fun pdfWebClient(): WebClient {
        return webClientBuilder
            .baseUrl(pdfServiceURL)
            .clientConnector(ReactorClientHttpConnector(HttpClient.newConnection()))
            .build()
    }
}