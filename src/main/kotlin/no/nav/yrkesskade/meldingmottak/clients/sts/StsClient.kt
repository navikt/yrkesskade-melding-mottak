package no.nav.yrkesskade.meldingmottak.clients.sts

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.lang.invoke.MethodHandles

@Component
class StsClient(private val stsWebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
        private var cachedOidcToken: OidcToken? = null
    }

    @Retryable
    fun oidcToken(): String {
        if (cachedOidcToken.shouldBeRenewed()) {
            log.debug("Getting token from STS")
            cachedOidcToken = stsWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("scope", "openid")
                        .build()
                }
                .retrieve()
                .bodyToMono<OidcToken>()
                .block()
        }

        return cachedOidcToken!!.token
    }

    private fun OidcToken?.shouldBeRenewed(): Boolean = this?.hasExpired() ?: true
}
