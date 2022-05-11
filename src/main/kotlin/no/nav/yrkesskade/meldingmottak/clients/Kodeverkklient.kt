package no.nav.yrkesskade.meldingmottak.clients

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto
import no.nav.yrkesskade.kodeverk.model.KodeverdiResponsDto
import no.nav.yrkesskade.meldingmottak.domene.KodeverdiRespons
import no.nav.yrkesskade.meldingmottak.domene.KodeverkKode
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import org.slf4j.LoggerFactory
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
    fun hentKodeverk(type: String, kategori: String?, spraak: String = "nb"): Map<String, KodeverdiDto> {
        log.info("Kaller ys-kodeverk - type=$type, kategori=$kategori, spraak=$spraak")
        return logTimingAndWebClientResponseException("hentLand") {
            kallKodeverkApi(type, kategori)
        }
    }

    private fun kallKodeverkApi(type: String, kategori: String?): Map<String, KodeverdiDto> {
        val uriPath = if (kategori.isNullOrBlank()) "api/v1/kodeverk/typer/$type/kodeverdier" else "api/v1/kodeverk/typer/$type/kategorier/$kategori/kodeverdier"
        val kodeverdiRespons = kodeverkWebClient.get()
            .uri { uriBuilder -> uriBuilder.path(uriPath).build() }
            .retrieve()
            .bodyToMono<KodeverdiResponsDto>()
            .block() ?: KodeverdiResponsDto(emptyMap())
        // TODO: YSMOD-161 - Send med callid i header for enklere feilsøking

        return kodeverdiRespons.kodeverdierMap.orEmpty()
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