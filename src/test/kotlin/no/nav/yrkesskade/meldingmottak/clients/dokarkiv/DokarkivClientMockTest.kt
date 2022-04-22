package no.nav.yrkesskade.meldingmottak.clients.dokarkiv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.yrkesskade.meldingmottak.fixtures.opprettJournalpostOkRespons
import no.nav.yrkesskade.meldingmottak.fixtures.opprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
internal class DokarkivClientMockTest {

    private lateinit var dokarkivClient: DokarkivClient

    @MockK(relaxed = true)
    lateinit var tokenUtilMock: TokenUtil

    @Test
    fun `journalfoerSkademelding skal håndtere OK-respons`() {
        dokarkivClient = DokarkivClient(
            createShortCircuitWebClientWithStatus(
                jacksonObjectMapper().writeValueAsString(opprettJournalpostOkRespons()),
                HttpStatus.OK
            ),
            tokenUtilMock,
            "mock"
        )
        dokarkivClient.journalfoerDokument(opprettJournalpostRequest())
    }

    @Test
    fun `journalfoerSkademelding skal håndtere 409 CONFLICT-respons`() {
        dokarkivClient = DokarkivClient(
            createShortCircuitWebClientWithStatus("", HttpStatus.CONFLICT),
            tokenUtilMock,
            "mock"
        )
        dokarkivClient.journalfoerDokument(opprettJournalpostRequest())
    }
}

fun createShortCircuitWebClientWithStatus(jsonResponse: String, status: HttpStatus): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(status)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    return WebClient.builder()
        .exchangeFunction { Mono.just(clientResponse) }
        .build()
}
