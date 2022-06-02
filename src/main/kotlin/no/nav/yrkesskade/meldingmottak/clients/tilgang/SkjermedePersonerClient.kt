package no.nav.yrkesskade.meldingmottak.clients.tilgang

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
class SkjermedePersonerClient(
	private val skjermedePersonerWebClient: WebClient,
	private val tokenUtil: TokenUtil
) {

	companion object {
		@Suppress("JAVA_CLASS_ON_COMPANION")
		private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
		private val secureLogger = getSecureLogger()
	}

	@Retryable
	fun erSkjermet(request: SkjermedePersonerRequest): SkjermedePersonerResponse {
		secureLogger.info("Kontrollerer om personer er skjermet/egne ansatte: ${request.personIdenter}")
		return logTimingAndWebClientResponseException("erSkjermet") {
			skjermedePersonerWebClient.post()
				.uri { uriBuilder ->
					uriBuilder.pathSegment("skjermetBulk").build()
				}
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithSkjermedePersonerScope()}")
				.header("Nav-Callid", MDCConstants.MDC_CALL_ID)
//				.header("Nav-Consumer-Id", applicationName)
				.bodyValue(request)
				.retrieve()
				.bodyToMono<SkjermedePersonerResponse>()
				.block() ?: throw RuntimeException("Kunne ikke kontrollere om personer er skjermet")
		}
	}

	@Suppress("SameParameterValue")
	private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
		val start: Long = System.currentTimeMillis()
		try {
			return function.invoke()
		} catch (ex: WebClientResponseException) {
			secureLogger.error(
				"Klarte ikke å hente skjermet person bolk. Got a {} error calling Pdf Dokgen {} {} with message {}",
				ex.statusCode,
				ex.request?.method ?: "-",
				ex.request?.uri ?: "-",
				ex.responseBodyAsString
			)
			throw ex
		} catch (rtex: RuntimeException) {
			log.warn("Caught RuntimeException while calling Pdf Dokgen", rtex)
			throw rtex
		} finally {
			val end: Long = System.currentTimeMillis()
			log.info("Method {} took {} millis", methodName, (end - start))
		}
	}


	data class SkjermedePersonerRequest(
		val personIdenter: List<String>
	)

	data class SkjermedePersonerResponse(
		val skjermedePersonerMap: Map<String, Boolean>
	)
}
