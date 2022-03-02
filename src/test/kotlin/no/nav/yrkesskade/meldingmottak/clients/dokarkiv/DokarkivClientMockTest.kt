package no.nav.yrkesskade.meldingmottak.clients.dokarkiv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.yrkesskade.meldingmottak.fixtures.opprettJournalpostOkRespons
import no.nav.yrkesskade.meldingmottak.fixtures.opprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
internal class DokarkivClientMockTest {

    private lateinit var dokarkivClient: DokarkivClient

    @MockK(relaxed = true)
    lateinit var tokenUtilMock: TokenUtil

    @BeforeEach
    fun setUp() {
        every { tokenUtilMock.getAppAccessTokenWithSafScope() } returns "abc"
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `journalfoerSkademelding skal håndtere OK-respons`() {
        dokarkivClient = DokarkivClient(
            createShortCircuitWebClient(jacksonObjectMapper().writeValueAsString(opprettJournalpostOkRespons())),
            tokenUtilMock,
            "mock"
        )
        dokarkivClient.journalfoerSkademelding(opprettJournalpostRequest())
    }

    @Test
    fun `journalfoerSkademelding skal håndtere 409 CONFLICT-respons`() {
        dokarkivClient = DokarkivClient(
            createShortCircuitWebClientWithStatus("", HttpStatus.CONFLICT),
            tokenUtilMock,
            "mock"
        )
        dokarkivClient.journalfoerSkademelding(opprettJournalpostRequest())
    }
}

fun createShortCircuitWebClient(jsonResponse: String): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(HttpStatus.OK)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(clientResponse)
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}

fun createShortCircuitWebClientWithStatus(jsonResponse: String, status: HttpStatus): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(status)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(clientResponse)
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}
