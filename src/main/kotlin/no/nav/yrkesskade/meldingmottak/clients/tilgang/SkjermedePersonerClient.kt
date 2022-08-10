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

	@Suppress("SameParameterValue")
	private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
		val start: Long = System.currentTimeMillis()
		try {
			return function.invoke()
		} catch (ex: WebClientResponseException) {
			secureLogger.error(
				"Klarte ikke å hente skjermet person bolk. Got a {} error calling Skjermede Personer {} {} with message {}",
				ex.statusCode,
				ex.request?.method ?: "-",
				ex.request?.uri ?: "-",
				ex.responseBodyAsString
			)
			throw ex
		} catch (rtex: RuntimeException) {
			log.warn("Caught RuntimeException while calling Skjermede Personer", rtex)
			throw rtex
		} finally {
			val end: Long = System.currentTimeMillis()
			log.info("Method {} took {} millis", methodName, (end - start))
		}
	}


	data class SkjermetPersonRequest(
		val personident: String
	)

}
