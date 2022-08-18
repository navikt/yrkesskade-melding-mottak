package no.nav.yrkesskade.meldingmottak.clients.infotrygd

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import kotlin.test.assertFailsWith

@Suppress("PrivatePropertyName", "NonAsciiCharacters")
@ExtendWith(MockKExtension::class)
class InfotrygdClientTest {

    private var infotrygdWebClientMock: WebClient = mockk()

    private lateinit var client: InfotrygdClient

    @MockK(relaxed = true)
    lateinit var tokenUtilMock: TokenUtil

    private val `d-nummer` = "11111111111"
    private val foedselsnummer = "22222222222"

    @Test
    fun `skal returnere responsen når ys-infotrygd gir OK svar og svar er true`() {
        client = InfotrygdClient(
            createShortCircuitWebClientWithStatus(
                jacksonObjectMapper().writeValueAsString(opprettInfotrygdEksisterendeSakOKResponse(true)),
                HttpStatus.OK
            ),
            tokenUtilMock
        )
        assertThat(client.harEksisterendeSak(listOf(`d-nummer`, foedselsnummer))).isTrue
    }

    @Test
    fun `skal returnere responsen når ys-infotrygd gir OK svar og svar er false`() {
        client = InfotrygdClient(
            createShortCircuitWebClientWithStatus(
                jacksonObjectMapper().writeValueAsString(opprettInfotrygdEksisterendeSakOKResponse(false)),
                HttpStatus.OK
            ),
            tokenUtilMock
        )
        assertThat(client.harEksisterendeSak(listOf(`d-nummer`, foedselsnummer))).isFalse
    }

    @Test
    fun `skal kaste feil når ys-infotrygd gir error response`() {
        coEvery { infotrygdWebClientMock.post() } throws
                WebClientResponseException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "En feilmelding", null, null, null
                )

        client = InfotrygdClient(infotrygdWebClientMock, tokenUtilMock)

        val exception = assertFailsWith<WebClientResponseException>(
            block = {
                client.harEksisterendeSak(listOf(`d-nummer`, foedselsnummer))
            }
        )
        assertThat(exception.message).isEqualTo("500 En feilmelding")
    }

}

private fun opprettInfotrygdEksisterendeSakOKResponse(eksisterendeSak: Boolean): InfotrygdEksisterendeSakResponse =
    InfotrygdEksisterendeSakResponse(eksisterendeSak)

private fun createShortCircuitWebClientWithStatus(jsonResponse: String, status: HttpStatus): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(status)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    return WebClient.builder()
        .exchangeFunction { Mono.just(clientResponse) }
        .build()
}
