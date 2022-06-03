package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.Date
import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.hentperson.*
import io.mockk.every
import io.mockk.mockk
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RutingServiceTest {

    private val pdlClientMock: PdlClient = mockk()
    private val skjermedePersonerClient: SkjermedePersonerClient = mockk()

    lateinit var service: RutingService


    @BeforeEach
    fun init() {
        service = RutingService(pdlClientMock, skjermedePersonerClient)
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
        val foedselsnummer = "12345678901"
        every { skjermedePersonerClient.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(mapOf(foedselsnummer to true)) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isTrue
    }

    @Test
    fun `er ikke egen ansatt - fnr i resultatet`() {
        val foedselsnummer = "23456789012"
        every { skjermedePersonerClient.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(mapOf(foedselsnummer to false)) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isFalse
    }

    @Test
    fun `er ikke egen ansatt - fnr ikke i resultatet`() {
        val foedselsnummer = "34567890123"
        every { skjermedePersonerClient.erSkjermet(ofType(SkjermedePersonerClient.SkjermedePersonerRequest::class)) } answers
                { SkjermedePersonerClient.SkjermedePersonerResponse(emptyMap()) }

        assertThat(service.erEgenAnsatt(foedselsnummer)).isFalse
    }

//    @Test
//    fun `sak eksisterer`() {
//        assertThat(service.harEksisterendeSak("12345678901")).isTrue
//    }
//
//    @Test
//    fun `sak eksisterer ikke`() {
//        assertThat(service.harEksisterendeSak("23456789012")).isFalse
//    }



//    @Test
//    fun `hvis sak eksisterer i Infotrygd, rut til gammelt saksbehandlingssystem Gosys og Infotrygd`() {
//        val foedselsnummer = "12345678901"
//        service.utfoerRuting(foedselsnummer)
//
//        // spy i mockk
//    }
//
//    @Test
//    fun `hvis potensiell kommende sak, dvs nylig oppgave i Gosys, rut til gammelt saksbehandlingssystem Gosys og Infotrygd`() {
//
//    }
//
//    @Test
//    fun `hvis ingen eksisterende sak, rut til Yrkesskade saksbehandlingssystem`() {
//
//    }




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