package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.generated.HentIdenter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.yrkesskade.meldingmottak.fixtures.*
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockKExtension::class)
internal class PdlClientTest {

    private lateinit var client: PdlClient

    @MockK
    lateinit var graphQLWebClient: GraphQLWebClient

    @MockK(relaxed = true)
    lateinit var tokenUtilMock: TokenUtil


    @BeforeEach
    fun init() {
        every { tokenUtilMock.getAppAccessTokenWithSafScope() } returns "abc"

        client = PdlClient(pdlGraphqlUrl = "test", tokenUtilMock)
        ReflectionTestUtils.setField(client, "client", graphQLWebClient)
    }


    @Test
    fun `skal hente aktorId`() {
        coEvery { graphQLWebClient.execute<HentIdenter.Result>(any(), any()) } returns okResponsFraPdl()
        val aktorId = client.hentAktorId("12345678901")
        assertThat(aktorId).isEqualTo("12345")
    }

}