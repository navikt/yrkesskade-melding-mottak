package no.nav.yrkesskade.meldingmottak.clients

import no.nav.yrkesskade.meldingmottak.domene.Land
import no.nav.yrkesskade.meldingmottak.domene.Landkode
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class Kodeverkklient(
    private val kodeverkWebClient: WebClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Retryable
    fun hentLand(spraak: String = "nb"): Map<Landkode, Land> {
        log.info("Henter land fra ys-kodeverk")
        return logTimingAndWebClientResponseException("hentLand") {
            kodeverkWebClient.get()
                .uri { uriBuilder ->
                    val typeLandkoder = "landkoderISO2"
                    val tilfeldigKategori = "arbeidstaker"
                    uriBuilder.pathSegment("api/v1/kodeverk/typer", typeLandkoder, "kategorier", tilfeldigKategori, "kodeverdier")
                        .build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono<Map<Landkode, Land>>()
                .block() ?: emptyMap()
        }
    }

    @Suppress("SameParameterValue")
    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            log.error(
                "Got a {} error calling kodeverk {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            log.error("Caught RuntimeException while calling kodeverk ", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }

}