package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.Journalposter
import com.expediagroup.graphql.generated.Saker
import com.expediagroup.graphql.generated.enums.Journalstatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.fixtures.*
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils

@Suppress("NonAsciiCharacters")
@ExtendWith(MockKExtension::class)
internal class SafClientTest {

    private lateinit var client: SafClient

    @MockK
    lateinit var graphQLWebClient: GraphQLWebClient

    @MockK(relaxed = true)
    lateinit var tokenUtilMock: TokenUtil

    @BeforeEach
    fun init() {
        every { tokenUtilMock.getAppAccessTokenWithSafScope() } returns "abc"

        client = SafClient(safGraphqlUrl = "test", "yrkesskade-melding-mottak", tokenUtilMock)
        ReflectionTestUtils.setField(client, "client", graphQLWebClient)
    }

    @Test
    fun `hentOppdatertJournalpost skal kaste exception naar SAF gir error response`() {
        coEvery { graphQLWebClient.execute<Journalpost.Result>(any(), any()) } returns errorJournalpostRespons()
        Assertions.assertThrows(RuntimeException::class.java) {
            client.hentOppdatertJournalpost("123123")
        }
    }

    @Test
    fun `hentOppdatertJournalpost skal returnere responsen naar SAF gir ok til svar`() {
        coEvery { graphQLWebClient.execute<Journalpost.Result>(any(), any()) } returns okJournalpostRespons()
        val journalpost = client.hentOppdatertJournalpost("1337")
        assertThat(journalpost).isEqualTo(journalpostResultWithBrukerAktoerid())
    }

    @Test
    fun `hentJournalposterforPerson skal kaste exception naar SAF gir error response`() {
        coEvery { graphQLWebClient.execute<Journalposter.Result>(any(), any()) } returns errorJournalposterRespons()
        Assertions.assertThrows(RuntimeException::class.java) {
            client.hentJournalposterForPerson("123123", listOf(Journalstatus.MOTTATT, Journalstatus.UNDER_ARBEID))
        }
    }

    @Test
    fun `hentJournalposterForPerson skal returnere responsen naar SAF gir ok til svar`() {
        coEvery { graphQLWebClient.execute<Journalposter.Result>(any(), any()) } returns okJournalposterRespons()
        val journalposter = client.hentJournalposterForPerson("1337", listOf(Journalstatus.MOTTATT, Journalstatus.MOTTATT))
        assertThat(journalposter).isEqualTo(journalposterResult())
    }

    @Test
    fun `hentSakerForPerson skal kaste exception når saf gir error response`() {
        coEvery { graphQLWebClient.execute<Saker.Result>(any(), any()) } returns errorSakerRespons()
        Assertions.assertThrows(java.lang.RuntimeException::class.java) {
            client.hentSakerForPerson("12345678901")
        }
    }

    @Test
    fun `skal hente saker for en person når saf gir ok til svar`() {
        coEvery { graphQLWebClient.execute<Saker.Result>(any(), any()) } returns okSakerResponse()
        val saker = client.hentSakerForPerson("12345678901")
        assertThat(saker).isEqualTo(sakerResultMedGenerellYrkesskadesak())
    }

}