package no.nav.yrkesskade.meldingmottak.clients.tilgang

import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.clients.AbstractRestClient
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SkjermedePersonerClient(
	private val skjermedePersonerWebClient: WebClient,
	private val tokenUtil: TokenUtil
) : AbstractRestClient("Skjermede personer") {

	companion object {
		@Suppress("JAVA_CLASS_ON_COMPANION")
		private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
		private val secureLogger = getSecureLogger()
	}

	@Retryable
	fun erSkjermet(request: SkjermetPersonRequest): Boolean {
		secureLogger.info("Kontrollerer om person er skjermet/egen ansatt: ${request.personident}")
		return logTimingAndWebClientResponseException("erSkjermet") {
			skjermedePersonerWebClient.post()
				.uri { uriBuilder ->
					uriBuilder.pathSegment("skjermet").build()
				}
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithSkjermedePersonerScope()}")
				.header("Nav-Callid", MDCConstants.MDC_CALL_ID)
				.bodyValue(request)
				.retrieve()
				.bodyToMono<Boolean>()
				.block() ?: throw RuntimeException("Kunne ikke kontrollere om person er skjermet")
		}
	}
}


data class SkjermetPersonRequest(
	val personident: String
)
