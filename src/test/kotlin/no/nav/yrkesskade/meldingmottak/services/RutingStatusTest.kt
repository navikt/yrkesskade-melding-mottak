package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.services.RutingService.RutingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RutingStatusTest {

    @Test
    fun `resultatSomTekst naar ingen sjekker har slaatt til`() {
        val forventet =
            """
            Rutingstatus for person:
            ------------------------------------------
            Finnes ikke i PDL                            [Nei]
            Er død                                       [Nei]
            Er fortrolig (kode 7)                        [Nei]
            Er strengt fortrolig (kode 6)                [Nei]
            Er egen ansatt/skjermet person               [Nei]
            Har åpen generell YRK-sak                    [Nei]
            Har eksisterende Infotrygd-sak               [Nei]
            Har potensiell kommende sak                  [Nei]
            Ingen av sjekkene har slått til, bruk default ruting
            Resultat:  YRKESSKADE_SAKSBEHANDLING
            """.trimIndent()

        val rutingStatus = RutingStatus()
        assertThat(rutingStatus.resultatSomTekst()).isEqualTo(forventet)
    }

    @Test
    fun `resultatSomTekst naar person ikke finnes i PDL`() {
        val forventet =
            """
            Rutingstatus for person:
            ------------------------------------------
            Finnes ikke i PDL                             [Ja]
            Er død                                       [Nei]
            Er fortrolig (kode 7)                        [Nei]
            Er strengt fortrolig (kode 6)                [Nei]
            Er egen ansatt/skjermet person               [Nei]
            Har åpen generell YRK-sak                    [Nei]
            Har eksisterende Infotrygd-sak               [Nei]
            Har potensiell kommende sak                  [Nei]
            Resultat:  GOSYS_OG_INFOTRYGD
            """.trimIndent()

        val rutingStatus = RutingStatus(finnesIkkeIPdl = true, rutingResult = RutingService.Rute.GOSYS_OG_INFOTRYGD)
        assertThat(rutingStatus.resultatSomTekst()).isEqualTo(forventet)
    }

    @Test
    fun `tekst naar person er doed`() {
        assertThat(RutingStatus(doed = true).resultatSomTekst()).contains("Er død                                        [Ja]")
    }

    @Test
    fun `tekst naar person er fortrolig - kode 7`() {
        assertThat(RutingStatus(kode7Fortrolig = true).resultatSomTekst()).contains("Er fortrolig (kode 7)                         [Ja]")
    }

    @Test
    fun `tekst naar person er strengt fortrolig - kode 6`() {
        assertThat(RutingStatus(kode6StrengtFortrolig = true).resultatSomTekst()).contains("Er strengt fortrolig (kode 6)                 [Ja]")
    }

    @Test
    fun `tekst naar person er egen ansatt - skjermet person`() {
        assertThat(RutingStatus(egenAnsatt = true).resultatSomTekst()).contains("Er egen ansatt/skjermet person                [Ja]")
    }

    @Test
    fun `tekst naar person har aapen generell YRK-sak`() {
        assertThat(RutingStatus(aapenGenerellYrkesskadeSak = true).resultatSomTekst()).contains("Har åpen generell YRK-sak                     [Ja]")
    }

    @Test
    fun `tekst naar person har eksisterende Infotrygd-sak`() {
        assertThat(RutingStatus(eksisterendeInfotrygdSak = true).resultatSomTekst()).contains("Har eksisterende Infotrygd-sak                [Ja]")
    }

    @Test
    fun `tekst naar person har potensiell kommende sak`() {
        assertThat(RutingStatus(potensiellKommendeSak = true).resultatSomTekst()).contains("Har potensiell kommende sak                   [Ja]")
    }

}