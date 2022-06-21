package no.nav.yrkesskade.meldingmottak.clients.infotrygd

import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class InfotrygdClient(
    private val infotrygdWebClient: WebClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun harEksisterendeSak(fodselsnumre: List<String>): Boolean {
        log.info("InfotrygdClient: Sjekker om person har en eksisterende sak")
        secureLogger.info("Client: Sjekker om personen $fodselsnumre har en eksisterende sak")
        val eksisterendeSakResponse = logTimingAndWebClientResponseException("harEksisterendeSak") {
            infotrygdWebClient.post()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("infotrygd")
                        .pathSegment("sak")
                        .pathSegment("eksisterende-sak")
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fodselsnumre)
                .retrieve()
                .bodyToMono<InfotrygdEksisterendeSakResponse>()
                .block() ?: throw RuntimeException("Kunne ikke sjekke om eksisterende sak")
        }
        return eksisterendeSakResponse.harEksisterendeSak
            .also {
                log.info("InfotrygdClient: harEksisterendeSak = ${eksisterendeSakResponse.harEksisterendeSak}")
            }
    }

    @Suppress("SameParameterValue")
    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling YS-Infotrygd {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            log.warn("Caught RuntimeException while calling YS-Infotrygd", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}


data class InfotrygdEksisterendeSakResponse(val harEksisterendeSak: Boolean)
