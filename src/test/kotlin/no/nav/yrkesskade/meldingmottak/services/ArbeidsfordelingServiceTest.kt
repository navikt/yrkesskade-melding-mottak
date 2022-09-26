package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.ArbeidsfordelingClient
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.ArbeidsfordelingKriterie
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.ArbeidsfordelingResponse
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.EnhetResponse
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import no.nav.yrkesskade.meldingmottak.fixtures.gyldigFortroligPersonMedNavnOgVegadresse
import no.nav.yrkesskade.meldingmottak.fixtures.gyldigPersonFra
import no.nav.yrkesskade.meldingmottak.fixtures.gyldigPersonMedNavnOgVegadresse
import no.nav.yrkesskade.meldingmottak.fixtures.gyldigStrengtFortroligPersonMedNavnOgVegadresse
import no.nav.yrkesskade.meldingmottak.services.ArbeidsfordelingService.Companion.KODE_6_STRENGT_FORTROLIG
import no.nav.yrkesskade.meldingmottak.services.ArbeidsfordelingService.Companion.KODE_7_FORTROLIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
internal class ArbeidsfordelingServiceTest {

    companion object {
        private val DEFAULT_ENHET = Enhet("0", "Default Enhet")
        private val ENHET_SOER = Enhet("1", "Enhet Sør")
        private val ENHET_NORD = Enhet("2", "Enhet Nord")
        private val ENHET_FORTROLIG = Enhet("007", "Enhet Hysj")
        private val ENHET_STRENGT_FORTROLIG = Enhet("006", "Enhet Hysj-hysj")
        private val ENHET_SKJERMING = Enhet("5", "Enhet Skjerming")

        private const val AAS_KOMMUNE = "3021"
        private const val KARASJOK_KOMMUNE = "5437"
    }

    private val arbeidsfordelingClientMock: ArbeidsfordelingClient = mockk()
    private val pdlClientMock: PdlClient = mockk()
    private val skjermedePersonerClientMock: SkjermedePersonerClient = mockk()

    private val arbeidsfordelingService =
        ArbeidsfordelingService(
            arbeidsfordelingClient = arbeidsfordelingClientMock,
            pdlClient = pdlClientMock,
            skjermedePersonerClient = skjermedePersonerClientMock
        )

    @BeforeEach
    fun setup() {
        // Personer fra PDL er uten kommunenummer og uten diskresjonskode, dersom det ikke overstyres i tester
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonMedNavnOgVegadresse()

        // Personer er ikke skjermet (egne ansatte), dersom det ikke overstyres i tester
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns false


        // Oppsett av en del tenkte svar fra NORG2:

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", null, null, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("0", "Default Enhet")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", AAS_KOMMUNE, null, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("1", "Enhet Sør")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", KARASJOK_KOMMUNE, null, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("2", "Enhet Nord")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", null, KODE_7_FORTROLIG, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("007", "Enhet Hysj")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", KARASJOK_KOMMUNE, KODE_7_FORTROLIG, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("007", "Enhet Hysj")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", null, KODE_6_STRENGT_FORTROLIG, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("006", "Enhet Hysj-hysj")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", AAS_KOMMUNE, KODE_6_STRENGT_FORTROLIG, false))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("006", "Enhet Hysj-hysj")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", null, null, true))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("5", "Enhet Skjerming")))

        every { arbeidsfordelingClientMock.finnBehandlendeEnhetMedBesteMatch(eq(ArbeidsfordelingKriterie("YRK", KARASJOK_KOMMUNE, null, true))) } returns
                ArbeidsfordelingResponse(listOf(EnhetResponse("5", "Enhet Skjerming")))
    }

    @Test
    fun `skal finne behandlende enhet for person med kommunenr`() {
        every { pdlClientMock.hentPerson(eq("11111111111")) } returns gyldigPersonFra(AAS_KOMMUNE)
        every { pdlClientMock.hentPerson(eq("22222222222")) } returns gyldigPersonFra(KARASJOK_KOMMUNE)

        val enhetForPersonFraSoer = arbeidsfordelingService.finnBehandlendeEnhetForPerson("11111111111")
        assertThat(enhetForPersonFraSoer).isEqualTo(ENHET_SOER)

        val enhetforPersonFraNord = arbeidsfordelingService.finnBehandlendeEnhetForPerson("22222222222")
        assertThat(enhetforPersonFraNord).isEqualTo(ENHET_NORD)
    }

    @Test
    fun `skal finne default behandlende enhet for person uten kommunenr eller diskresjonskode`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonFra(null)

        val enhetForUkjentKommune = arbeidsfordelingService.finnBehandlendeEnhetForPerson("99999999999")
        assertThat(enhetForUkjentKommune).isEqualTo(DEFAULT_ENHET)
    }

    @Test
    fun `skal finne kommune for en person med vegadresse`() {
        val geografiskOmraade = arbeidsfordelingService.geografiskOmraade(gyldigPersonFra(KARASJOK_KOMMUNE))
        assertThat(geografiskOmraade).isEqualTo(KARASJOK_KOMMUNE)
    }

    @Test
    fun `ingen geografisk område for en person med ukjent kommune`() {
        val geografiskOmraade = arbeidsfordelingService.geografiskOmraade(gyldigPersonFra(null))
        assertThat(geografiskOmraade).isNull()
    }

    @Test
    fun `skal finne behandlende enhet for person med diskresjonskode fortrolig`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigFortroligPersonMedNavnOgVegadresse()

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("33333333333")
        assertThat(enhet).isEqualTo(ENHET_FORTROLIG)
    }

    @Test
    fun `skal finne fortrolig enhet for person med både kommunenr og diskresjonskode fortrolig`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigFortroligPersonMedNavnOgVegadresse(KARASJOK_KOMMUNE)

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("44444444444")
        assertThat(enhet).isEqualTo(ENHET_FORTROLIG)
    }

    @Test
    fun `skal finne behandlende enhet for person med diskresjonskode strengt fortrolig`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigStrengtFortroligPersonMedNavnOgVegadresse()

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("55555555555")
        assertThat(enhet).isEqualTo(ENHET_STRENGT_FORTROLIG)
    }

    @Test
    fun `skal finne strengt fortrolig enhet for person med både kommunenr og diskresjonskode strengt fortrolig`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigStrengtFortroligPersonMedNavnOgVegadresse(AAS_KOMMUNE)

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("66666666666")
        assertThat(enhet).isEqualTo(ENHET_STRENGT_FORTROLIG)
    }

    @Test
    fun `skal finne behandlende enhet for skjermet person (egen ansatt)`() {
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns true

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("77777777777")
        assertThat(enhet).isEqualTo(ENHET_SKJERMING)
    }

    @Test
    fun `skal finne skjerming enhet for person med både kommunenr og skjermet person (egen ansatt)`() {
        every { pdlClientMock.hentPerson(any()) } returns gyldigPersonFra(KARASJOK_KOMMUNE)
        every { skjermedePersonerClientMock.erSkjermet(any()) } returns true

        val enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson("88888888888")
        assertThat(enhet).isEqualTo(ENHET_SKJERMING)
    }

}