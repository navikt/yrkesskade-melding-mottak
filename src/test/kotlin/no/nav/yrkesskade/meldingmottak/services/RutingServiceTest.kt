package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.Date
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.hentperson.*
import io.mockk.every
import io.mockk.mockk
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.clients.infotrygd.InfotrygdClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import no.nav.yrkesskade.meldingmottak.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class RutingServiceTest {

    private val pdlClientMock: PdlClient = mockk()
    private val safClientMock: SafClient = mockk()
    private val skjermedePersonerClientMock: SkjermedePersonerClient = mockk()
    private val infotrygdClientMock: InfotrygdClient = mockk()

    private lateinit var service: RutingService

    private val foedselsnummer = "12345678901"


    @BeforeEach
    fun init() {
        service = RutingService(pdlClientMock, safClientMock, skjermedePersonerClientMock, infotrygdClientMock)
    }

    @Test
    fun `er doed`() {
        assertThat(service.erDoed(PersonBuilder().doedsfall("2022-15-20").build())).isTrue
        assertThat(service.erDoed(PersonBuilder().doedsfallUtenDato().build())).isTrue
    }

    @Test
    fun `er levende`() {
        val person = Person(emptyList(), emptyList(), emptyList(), emptyList())
        assertThat(service.erDoed(person)).isFalse
    }

    @Test
    fun `er kode 7 fortrolig`() {
        assertThat(service.erKode7Fortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG).build())).isTrue
    }

    @Test
    fun `er ikke kode 7 fortrolig`() {
        assertThat(service.erKode7Fortrolig(PersonBuilder().build())).isFalse
        assertThat(service.erKode7Fortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG).build())).isFalse
        assertThat(service.erKode7Fortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).build())).isFalse
        assertThat(service.erKode7Fortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT).build())).isFalse
        assertThat(service.erKode7Fortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.__UNKNOWN_VALUE).build())).isFalse
    }

    @Test
    fun `er kode 6 strengt fortrolig`() {
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG).build())).isTrue
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).build())).isTrue
    }

    @Test
    fun `er ikke kode 6 strengt fortrolig`() {
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().build())).isFalse
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG).build())).isFalse
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT).build())).isFalse
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.__UNKNOWN_VALUE).build())).isFalse
    }

    @Test
    fun `er egen ansatt, dvs skjermet person`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(mapOf(foedselsnummer to true)) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isTrue
    }

    @Test
    fun `er ikke egen ansatt - variant fnr i resultatet`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(mapOf(foedselsnummer to false)) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isFalse
    }

    @Test
    fun `er ikke egen ansatt - variant fnr ikke i resultatet`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(emptyMap()) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isFalse
    }

    @Test
    fun `skal returnere liste med foedselsnumre hvis hentIdenter har verdier`() {
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()

        assertThat(service.hentFoedselsnumreMedHistorikk("33333333333")).isEqualTo(listOf("11111111111", "22222222222", "33333333333"))
    }

    @Test
    fun `skal returnere liste med innsendt foedselsnummer hvis hentIdenter ikke har verdier`() {
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns HentIdenter.Result(null)

        assertThat(service.hentFoedselsnumreMedHistorikk("33333333333")).isEqualTo(listOf("33333333333"))
    }

    @Test
    fun `infotrygdsak eksisterer`() {
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns true

        assertThat(service.harEksisterendeInfotrygdSak(listOf("11111111111", "33333333333"))).isTrue
    }

    @Test
    fun `infotrygdsak eksisterer ikke`() {
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false

        assertThat(service.harEksisterendeInfotrygdSak(listOf("00000000000", "99999999999"))).isFalse
    }




    /* Test av ruting */

    @Test
    fun `hvis ingen person i pdl, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns null
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er død, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns doedPerson()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er kode 7 fortrolig, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigFortroligPersonMedNavnOgVegadresse()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er kode 6 strengt fortrolig, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigStrengtFortroligPersonMedNavnOgVegadresse()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er egen ansatt, dvs ansatt i NAV, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns SkjermedePersonerClient.SkjermedePersonerResponse(
            mapOf(foedselsnummer to true)
        )
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis åpen generell YRK sak eksisterer, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns SkjermedePersonerClient.SkjermedePersonerResponse(
            mapOf(foedselsnummer to false)
        )
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResultMedGenerellYrkesskadesak()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis sak eksisterer i Infotrygd, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns SkjermedePersonerClient.SkjermedePersonerResponse(
            mapOf(foedselsnummer to false)
        )
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns true
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis potensiell kommende sak, dvs nylig oppgave i Gosys, rut til gammelt saksbehandlingssystem Gosys og Infotrygd`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns SkjermedePersonerClient.SkjermedePersonerResponse(
            mapOf(foedselsnummer to false)
        )
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false
        every { safClientMock.hentJournalposterForPerson(any()) } returns journalposterResult()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis ingen ingen eksisterende eller kommende sak, rut til Yrkesskade saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns SkjermedePersonerClient.SkjermedePersonerResponse(
            mapOf(foedselsnummer to false)
        )
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false
        every { safClientMock.hentJournalposterForPerson(any()) } returns journalposterResultMedSak()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(RutingService.Rute.GOSYS_OG_INFOTRYGD) // TODO: 01/07/2022 YSMOD-375 Skal rute til ny saksbehandling når det er klart
    }




    data class PersonBuilder(
        var adressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
        var navn: List<Navn> = emptyList(),
        var doedsfall: List<Doedsfall> = emptyList(),
        var bostedsadresse: List<Bostedsadresse> = emptyList()
    ) {
        fun adressebeskyttelse(gradering: AdressebeskyttelseGradering) = apply { this.adressebeskyttelse = listOf(Adressebeskyttelse(gradering))}
        fun doedsfallUtenDato() = apply { this.doedsfall = listOf(Doedsfall(null)) }
        fun doedsfall(doedsdato: Date) = apply { this.doedsfall = listOf(Doedsfall(doedsdato)) }
        fun build() = Person(adressebeskyttelse, navn, doedsfall, bostedsadresse)
    }



}