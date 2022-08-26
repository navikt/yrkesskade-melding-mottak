package no.nav.yrkesskade.meldingmottak.clients.infotrygd

import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
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
    private val infotrygdWebClient: WebClient,
    private val tokenUtil: TokenUtil
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
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithYrkesskadeInfotrygdScope()}")
                .header("Nav-Callid", MDCConstants.MDC_CALL_ID)
                .bodyValue(InfotrygdEksisterendeSakRequest(fodselsnumre))
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


/**
 * @param foedselsnumre vil typisk være alle historiske pluss aktivt fødselsnummer for en person
 */
data class InfotrygdEksisterendeSakRequest(val foedselsnumre: List<String>)

data class InfotrygdEksisterendeSakResponse(val harEksisterendeSak: Boolean)
