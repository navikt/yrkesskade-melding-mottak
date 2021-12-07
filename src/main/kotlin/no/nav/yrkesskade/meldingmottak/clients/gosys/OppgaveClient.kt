package no.nav.yrkesskade.meldingmottak.clients.gosys

import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Retryable
    fun opprettOppgave(oppgave: OpprettJournalfoeringOppgave): Oppgave {
        log.info("Oppretter oppgave for journalpostId ${oppgave.journalpostId}")
        return oppgaveWebClient.post()
            .uri { uriBuilder ->
                uriBuilder.pathSegment("Oppgave")
                    .pathSegment("opprettOppgave")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithOppgaveScope()}")
            .header("X-Correlation-ID", UUID.randomUUID().toString())
            .header("Nav-Consumer-Id", applicationName)
            .bodyValue(oppgave)
            .retrieve()
            .bodyToMono<Oppgave>()
            .block() ?: throw RuntimeException("Kunne ikke lage oppgave")
    }
}
