package no.nav.yrkesskade.meldingmottak.clients.gosys

import com.expediagroup.graphql.generated.enums.Tema
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun opprettOppgave(oppgave: OpprettJournalfoeringOppgave): Oppgave {
        log.info("Oppretter oppgave for journalpostId ${oppgave.journalpostId}")
        return logTimingAndWebClientResponseException("opprettOppgave") {
            oppgaveWebClient.post()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("api")
                        .pathSegment("v1")
                        .pathSegment("oppgaver")
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithOppgaveScope()}")
                .header("X-Correlation-ID", MDCConstants.MDC_CALL_ID)
                .header("Nav-Consumer-Id", applicationName)
                .bodyValue(oppgave)
                .retrieve()
                .bodyToMono<Oppgave>()
                .block() ?: throw RuntimeException("Kunne ikke lage oppgave")
        }
    }

    fun finnOppgaver(journalpostId: String, oppgavetype: Oppgavetype): OppgaveResponse {
        log.info("Søker etter aktive oppgaver for $journalpostId")

        return logTimingAndWebClientResponseException("finnOppgaver") {
            oppgaveWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("api")
                        .pathSegment("v1")
                        .pathSegment("oppgaver")
//                        .queryParam("statuskategori", Statuskategori.AAPEN.toString())
                        .queryParam("tema", Tema.YRK.toString())
                        .queryParam("oppgavetype", oppgavetype.kortnavn)
                        .queryParam("journalpostId", journalpostId)
                        .build()
                }
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithOppgaveScope()}")
                .header("X-Correlation-ID", MDCConstants.MDC_CALL_ID)
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<OppgaveResponse>()
                .block() ?: throw RuntimeException("Kunne ikke hente oppgave")
        }
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling Oppgave {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
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
