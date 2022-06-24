package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.generated.HentAdresse
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.HentPerson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
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
        coEvery { graphQLWebClient.execute<HentIdenter.Result>(any(), any()) } returns okResponsIdenterFraPdl()
        val aktorId = client.hentAktorId("12345678901")
        assertThat(aktorId).isEqualTo("12345")
    }

    @Test
    fun `skal hente navn paa person`() {
        coEvery { graphQLWebClient.execute<HentPerson.Result>(any(), any()) } returns okResponsPersonFraPdl()
        val navn = client.hentNavn("12345678901")
        assertThat(navn?.fornavn).isEqualTo("Ola")
        assertThat(navn?.mellomnavn).isNull()
        assertThat(navn?.etternavn).isEqualTo("Normann")
    }

    @Test
    fun `skal hente navn og bostedsadresse paa person`() {
        coEvery { graphQLWebClient.execute<HentPerson.Result>(ofType(HentPerson::class), any()) } returns okResponsPersonFraPdl()
        coEvery { graphQLWebClient.execute<HentAdresse.Result>(ofType(HentAdresse::class), any()) } returns okResponsAdresseFraPdl()
        val navnOgAdresse = client.hentNavnOgAdresse("12345678901", true)

        val navn = navnOgAdresse.first
        assertThat(navn?.fornavn).isEqualTo("Ola")
        assertThat(navn?.mellomnavn).isNull()
        assertThat(navn?.etternavn).isEqualTo("Normann")

        val adresse = navnOgAdresse.second
        assertThat(adresse?.adresselinje1).isEqualTo("Storgata 123B")
        assertThat(adresse?.adresselinje2).isEqualTo("2250 Plassen")
        assertThat(adresse?.adresselinje3).isEqualTo("Tillegg")
        assertThat(adresse?.land).isEqualTo("")
    }

    @Test
    fun `bostedsadresse på person mangler`() {
        coEvery { graphQLWebClient.execute<HentPerson.Result>(ofType(HentPerson::class), any()) } returns okResponsPersonUtenBostedsadresseFraPdl()
        coEvery { graphQLWebClient.execute<HentAdresse.Result>(ofType(HentAdresse::class), any()) } returns okResponsAdresseFraPdl()
        val navnOgAdresse = client.hentNavnOgAdresse("12345678901", true)

        val navn = navnOgAdresse.first
        assertThat(navn?.fornavn).isEqualTo("Kari")
        assertThat(navn?.mellomnavn).isEqualTo("Storm")
        assertThat(navn?.etternavn).isEqualTo("Hansen")

        val adresse = navnOgAdresse.second
        assertThat(adresse).isNull()
    }

    @Test
    fun `skal hente adresse for kode7-personer`() {
        coEvery { graphQLWebClient.execute<HentPerson.Result>(ofType(HentPerson::class), any()) } returns okResponsFortroligPersonFraPdl()
        coEvery { graphQLWebClient.execute<HentAdresse.Result>(ofType(HentAdresse::class), any()) } returns okResponsAdresseFraPdl()
        val navnOgAdresse = client.hentNavnOgAdresse("12345678901", true)

        val adresse = navnOgAdresse.second
        assertThat(adresse?.adresselinje1).isEqualTo("Storgata 123B")
        assertThat(adresse?.adresselinje2).isEqualTo("2250 Plassen")
        assertThat(adresse?.adresselinje3).isEqualTo("Tillegg")
        assertThat(adresse?.land).isEqualTo("")
    }

    @Test
    fun `skal hente adresse for kode6-personer`() {
        coEvery { graphQLWebClient.execute<HentPerson.Result>(ofType(HentPerson::class), any()) } returns okResponsStrengtFortroligPersonFraPdl()
        coEvery { graphQLWebClient.execute<HentAdresse.Result>(ofType(HentAdresse::class), any()) } returns okResponsAdresseFraPdl()
        val navnOgAdresse = client.hentNavnOgAdresse("12345678901", true)

        val adresse = navnOgAdresse.second
        assertThat(adresse?.adresselinje1).isEqualTo("Storgata 123B")
        assertThat(adresse?.adresselinje2).isEqualTo("2250 Plassen")
        assertThat(adresse?.adresselinje3).isEqualTo("Tillegg")
        assertThat(adresse?.land).isEqualTo("")
    }

}