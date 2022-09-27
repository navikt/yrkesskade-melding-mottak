package no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import kotlin.test.assertFailsWith

@Suppress("NonAsciiCharacters")
@ExtendWith(MockKExtension::class)
class ArbeidsfordelingClientTest {

    private var webClientMock: WebClient = mockk()

    private lateinit var client: ArbeidsfordelingClient


    @Test
    fun `skal returnere responsen når NORG2 gir OK svar`() {
        client = ArbeidsfordelingClient(
            createShortCircuitWebClientWithStatus(
                jsonResponse(),
                HttpStatus.OK
            ),
            "Arbeidsfordeling Client")

        val kriterie = ArbeidsfordelingKriterie(
            tema = "YRK",
            geografiskOmraade = "3021",
            diskresjonskode = null,
            skjermet = false
        )

        val response = client.finnBehandlendeEnhetMedBesteMatch(kriterie)

        assertThat(response.enheter).hasSize(1)
        assertThat(response.enheter.first()).isEqualTo(EnhetResponse("4833", "NAV Familie- og pensjonsytelser Oslo 1"))
    }

    @Test
    fun `skal takle tom respons`() {
        client = ArbeidsfordelingClient(
            createShortCircuitWebClientWithStatus(
                tomJsonResponse(),
                HttpStatus.OK
            ),
            "Arbeidsfordeling Client")

        val response = client.finnBehandlendeEnhetMedBesteMatch(ArbeidsfordelingKriterie())

        assertThat(response.enheter).isEmpty()
    }

    @Test
    fun `skal kaste feil når NORG2 gir error response`() {
        coEvery { webClientMock.post() } throws
                WebClientResponseException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "En feilmelding", null, null, null
                )

        client = ArbeidsfordelingClient(webClientMock, "Arbeidsfordeling Client")

        val exception = assertFailsWith<WebClientResponseException>(
            block = {
                client.finnBehandlendeEnhetMedBesteMatch(ArbeidsfordelingKriterie())
            }
        )
        assertThat(exception.message).isEqualTo("500 En feilmelding")
    }


    private fun jsonResponse(): String {
        return """
        [
          {
            "enhetId": 100001595,
            "navn": "NAV Familie- og pensjonsytelser Oslo 1",
            "enhetNr": "4833",
            "antallRessurser": 0,
            "status": "Aktiv",
            "orgNivaa": "EN",
            "type": "FPY",
            "organisasjonsnummer": null,
            "underEtableringDato": "1970-01-01",
            "aktiveringsdato": "1970-01-01",
            "underAvviklingDato": null,
            "nedleggelsesdato": null,
            "oppgavebehandler": true,
            "versjon": 2,
            "sosialeTjenester": null,
            "kanalstrategi": null,
            "orgNrTilKommunaltNavKontor": null
          }
        ]
        """.trimIndent()
    }

    private fun tomJsonResponse(): String {
        return "[]"
    }

    private fun createShortCircuitWebClientWithStatus(jsonResponse: String, status: HttpStatus): WebClient {
        val clientResponse: ClientResponse = ClientResponse
            .create(status)
            .header("Content-Type", "application/json")
            .body(jsonResponse).build()

        return WebClient.builder()
            .exchangeFunction { Mono.just(clientResponse) }
            .build()
    }

}

