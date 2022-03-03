package no.nav.yrkesskade.meldingmottak.clients.dokarkiv

import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DokarkivClient(
    private val dokarkivWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun journalfoerSkademelding(opprettJournalpostRequest: OpprettJournalpostRequest): OpprettJournalpostResponse? {
        log.info("Journalfører skademelding")
        return logTimingAndWebClientResponseException("journalførSkademelding") {
            dokarkivWebClient.post()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("rest")
                        .pathSegment("journalpostapi")
                        .pathSegment("v1")
                        .pathSegment("journalpost")
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithDokarkivScope()}")
                .header("X-Correlation-ID", MDCConstants.MDC_CALL_ID)
                .header("Nav-Consumer-Id", applicationName)
                .bodyValue(opprettJournalpostRequest)
                .retrieve()
                .bodyToMono<OpprettJournalpostResponse>()
                .block() ?: throw RuntimeException("Kunne ikke journalføre skademelding")
        }
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T? {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling Dokarkiv {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            if (ex.statusCode == HttpStatus.CONFLICT) {
                log.info("Skademeldingen har allerede blitt journalført.")
                return null
            }
            throw ex
        } catch (rtex: RuntimeException) {
            log.warn("Caught RuntimeException", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}