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
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermetPersonRequest
import no.nav.yrkesskade.meldingmottak.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class EnhetsrutingServiceTest {

    private val pdlClientMock: PdlClient = mockk()
    private val safClientMock: SafClient = mockk()
    private val skjermedePersonerClientMock: SkjermedePersonerClient = mockk()
    private val infotrygdClientMock: InfotrygdClient = mockk()

    private lateinit var service: RutingService
    private lateinit var status: RutingStatus

    private val foedselsnummer = "12345678901"


    @BeforeEach
    fun init() {
        service = RutingService(pdlClientMock, safClientMock, skjermedePersonerClientMock, infotrygdClientMock)
        status = RutingStatus()
    }

    @Test
    fun `er doed`() {
        assertThat(service.erDoed(PersonBuilder().doedsfall("2022-15-20").build(), status)).isTrue
        assertThat(service.erDoed(PersonBuilder().doedsfallUtenDato().build(), status)).isTrue
    }

    @Test
    fun `er levende`() {
        val person = Person(emptyList(), emptyList(), emptyList(), emptyList())
        assertThat(service.erDoed(person, status)).isFalse
    }

    @Test
    fun `er kode 7 fortrolig`() {
        assertThat(service.erKode7Fortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG).build(),
            status
        )).isTrue
    }

    @Test
    fun `er ikke kode 7 fortrolig`() {
        assertThat(service.erKode7Fortrolig(PersonBuilder().build(), status)).isFalse
        assertThat(service.erKode7Fortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG).build(),
            status
        )).isFalse
        assertThat(service.erKode7Fortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).build(),
            status
        )).isFalse
        assertThat(service.erKode7Fortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT).build(),
            status
        )).isFalse
        assertThat(service.erKode7Fortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.__UNKNOWN_VALUE).build(),
            status
        )).isFalse
    }

    @Test
    fun `er kode 6 strengt fortrolig`() {
        assertThat(service.erKode6StrengtFortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG).build(),
            status
        )).isTrue
        assertThat(service.erKode6StrengtFortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).build(),
            status
        )).isTrue
    }

    @Test
    fun `er ikke kode 6 strengt fortrolig`() {
        assertThat(service.erKode6StrengtFortrolig(PersonBuilder().build(), status)).isFalse
        assertThat(service.erKode6StrengtFortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG).build(),
            status
        )).isFalse
        assertThat(service.erKode6StrengtFortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT).build(),
            status
        )).isFalse
        assertThat(service.erKode6StrengtFortrolig(
            PersonBuilder().adressebeskyttelse(AdressebeskyttelseGradering.__UNKNOWN_VALUE).build(),
            status
        )).isFalse
    }

    @Test
    fun `er egen ansatt, dvs skjermet person`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermetPersonRequest::class)) } answers
                { true }

        assertThat(service.erEgenAnsatt(foedselsnummer, status)).isTrue
    }

    @Test
    fun `er ikke egen ansatt - variant fnr i resultatet`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermetPersonRequest::class)) } answers
                { false }

        assertThat(service.erEgenAnsatt(foedselsnummer, status)).isFalse
    }

    @Test
    fun `er ikke egen ansatt - variant fnr ikke i resultatet`() {
        every { skjermedePersonerClientMock.erSkjermet(ofType(SkjermetPersonRequest::class)) } answers
                { false }

        assertThat(service.erEgenAnsatt(foedselsnummer, status)).isFalse
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

        assertThat(service.harEksisterendeInfotrygdSak(listOf("11111111111", "33333333333"), status)).isTrue
    }

    @Test
    fun `infotrygdsak eksisterer ikke`() {
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false

        assertThat(service.harEksisterendeInfotrygdSak(listOf("00000000000", "99999999999"), status)).isFalse
    }




    /* Test av ruting */

    @Test
    fun `hvis ingen person i pdl, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns null
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er død, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns doedPerson()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er kode 7 fortrolig, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigFortroligPersonMedNavnOgVegadresse()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er kode 6 strengt fortrolig, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigStrengtFortroligPersonMedNavnOgVegadresse()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis person er egen ansatt, dvs ansatt i NAV, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns true
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis åpen generell YRK sak eksisterer, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns false
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResultMedGenerellYrkesskadesak()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `skal hente åpne generelle YRK saker nyere enn 24 mnd`() {
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResultMedGenerellYrkesskadesak()
        assertThat(service.harAapenGenerellYrkesskadeSak("12345678901", RutingStatus())).isTrue
    }

    @Test
    fun `finner ingen åpen generell YRK sak naar saken er eldre enn 24 mnd`() {
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResultMedForGammelGenerellYrkesskadesak()
        assertThat(service.harAapenGenerellYrkesskadeSak("12345678901", RutingStatus())).isFalse
    }

    @Test
    fun `hvis sak eksisterer i Infotrygd, rut til gammelt saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns false
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns true
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `hvis potensiell kommende sak, dvs nylig oppgave i Gosys, rut til gammelt saksbehandlingssystem Gosys og Infotrygd`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns false
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false
        every { safClientMock.hentJournalposterForPerson(any(), any()) } returns journalposterResult()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `skal hente potensielle kommende saker naar det finnes journalpost nyere enn 24 mnd`() {
        every { safClientMock.hentJournalposterForPerson(any(), any()) } returns journalposterResult()
        assertThat(service.harPotensiellKommendeSak("12345678901", RutingStatus())).isTrue
    }

    @Test
    fun `finner ingen potensiell kommende sak naar journalposter er eldre enn 24 mnd`() {
        every { safClientMock.hentJournalposterForPerson(any(), any()) } returns forGamleJournalposterResult()
        assertThat(service.harPotensiellKommendeSak("12345678901", RutingStatus())).isFalse
    }

    @Test
    fun `hvis ingen ingen eksisterende eller kommende sak, rut til Yrkesskade saksbehandlingssystem`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns false
        every { pdlClientMock.hentIdenter(any(), any(), any()) } returns hentIdenterResultMedFnrHistorikk()
        every { safClientMock.hentSakerForPerson(any()) } returns sakerResult()
        every { infotrygdClientMock.harEksisterendeSak(any()) } returns false
        every { safClientMock.hentJournalposterForPerson(any(), any()) } returns journalposterResultMedSak()
        assertThat(service.utfoerRuting(foedselsnummer)).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)
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