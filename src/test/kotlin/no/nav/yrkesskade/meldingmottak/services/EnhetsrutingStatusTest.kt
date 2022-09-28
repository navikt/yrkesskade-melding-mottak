package no.nav.yrkesskade.meldingmottak.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
internal class EnhetsrutingStatusTest {

    @Test
    fun `resultatSomTekst naar ingen sjekker har slaatt til`() {
        val forventet =
            """
            Rutingstatus for person:
            ------------------------------------------
            Finnes ikke i PDL                            [Nei]
            Er død                                       [Nei]
            Har vergemål eller fremtidsfullmakt          [Nei]
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
            Har vergemål eller fremtidsfullmakt          [Nei]
            Er fortrolig (kode 7)                        [Nei]
            Er strengt fortrolig (kode 6)                [Nei]
            Er egen ansatt/skjermet person               [Nei]
            Har åpen generell YRK-sak                    [Nei]
            Har eksisterende Infotrygd-sak               [Nei]
            Har potensiell kommende sak                  [Nei]
            Resultat:  GOSYS_OG_INFOTRYGD
            """.trimIndent()

        val rutingStatus = RutingStatus(finnesIkkeIPdl = true, rutingResult = Rute.GOSYS_OG_INFOTRYGD)
        assertThat(rutingStatus.resultatSomTekst()).isEqualTo(forventet)
    }

    @Test
    fun `tekst naar person er doed`() {
        assertThat(RutingStatus(doed = true).resultatSomTekst()).contains("Er død                                        [Ja]")
    }

    @Test
    fun `tekst naar person har vergemål eller fremtidsfullmakt`() {
        assertThat(RutingStatus(harVergemaalEllerFremtidsfullmakt = true).resultatSomTekst()).contains("Har vergemål eller fremtidsfullmakt           [Ja]")
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

    @Test
    fun `resultat skal oppdateres naar Finnes ikke i PDL blir satt`() {
        val status = RutingStatus()
        assertThat(status.finnesIkkeIPdl).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.finnesIkkeIPdl = true
        assertThat(status.finnesIkkeIPdl).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Er død blir satt`() {
        val status = RutingStatus()
        assertThat(status.doed).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.doed = true
        assertThat(status.doed).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Har vergemål eller fremtidsfullmaktHar vergemål eller fremtidsfullmakt blir satt`() {
        val status = RutingStatus()
        assertThat(status.harVergemaalEllerFremtidsfullmakt).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.harVergemaalEllerFremtidsfullmakt = true
        assertThat(status.harVergemaalEllerFremtidsfullmakt).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Er fortrolig (kode 7) blir satt`() {
        val status = RutingStatus()
        assertThat(status.kode7Fortrolig).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.kode7Fortrolig = true
        assertThat(status.kode7Fortrolig).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Er strengt fortrolig (kode 6) blir satt`() {
        val status = RutingStatus()
        assertThat(status.kode6StrengtFortrolig).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.kode6StrengtFortrolig = true
        assertThat(status.kode6StrengtFortrolig).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Er egen ansatt slash skjermet person blir satt`() {
        val status = RutingStatus()
        assertThat(status.egenAnsatt).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.egenAnsatt = true
        assertThat(status.egenAnsatt).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Har åpen generell YRK-sak blir satt`() {
        val status = RutingStatus()
        assertThat(status.aapenGenerellYrkesskadeSak).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.aapenGenerellYrkesskadeSak = true
        assertThat(status.aapenGenerellYrkesskadeSak).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar Har eksisterende Infotrygd-sak blir satt`() {
        val status = RutingStatus()
        assertThat(status.eksisterendeInfotrygdSak).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.eksisterendeInfotrygdSak = true
        assertThat(status.eksisterendeInfotrygdSak).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `resultat skal oppdateres naar potensiell kommende sak blir satt`() {
        val status = RutingStatus()
        assertThat(status.potensiellKommendeSak).isFalse
        assertThat(status.rutingResult).isEqualTo(Rute.YRKESSKADE_SAKSBEHANDLING)

        status.potensiellKommendeSak = true
        assertThat(status.potensiellKommendeSak).isTrue
        assertThat(status.rutingResult).isEqualTo(Rute.GOSYS_OG_INFOTRYGD)
    }

    @Test
    fun `skal gi riktig rutingårsak`() {
        assertThat(RutingStatus(finnesIkkeIPdl = true).rutingAarsak()).isEqualTo(RutingAarsak.FINNES_IKKE_I_PDL)
        assertThat(RutingStatus(doed = true).rutingAarsak()).isEqualTo(RutingAarsak.DOED)
        assertThat(RutingStatus(harVergemaalEllerFremtidsfullmakt = true).rutingAarsak()).isEqualTo(RutingAarsak.VERGEMAAL_FREMTIDSFULLMAKT)
        assertThat(RutingStatus(kode7Fortrolig = true).rutingAarsak()).isEqualTo(RutingAarsak.KODE_7_FORTROLIG)
        assertThat(RutingStatus(kode6StrengtFortrolig = true).rutingAarsak()).isEqualTo(RutingAarsak.KODE_6_STRENGT_FORTROLIG)
        assertThat(RutingStatus(egenAnsatt = true).rutingAarsak()).isEqualTo(RutingAarsak.EGEN_ANSATT)
        assertThat(RutingStatus(aapenGenerellYrkesskadeSak = true).rutingAarsak()).isEqualTo(RutingAarsak.AAPEN_GENERELL_YRKESSKADESAK)
        assertThat(RutingStatus(eksisterendeInfotrygdSak = true).rutingAarsak()).isEqualTo(RutingAarsak.EKSISTERENDE_INFOTRYGDSAK)
        assertThat(RutingStatus(potensiellKommendeSak = true).rutingAarsak()).isEqualTo(RutingAarsak.POTENSIELL_KOMMENDE_SAK)
    }

    @Test
    fun `skal gi første rutingårsak når flere tilfeller slår til`() {
        val rutingStatus = RutingStatus(finnesIkkeIPdl = true, eksisterendeInfotrygdSak = true)
        assertThat(rutingStatus.rutingAarsak()).isNotEqualTo(RutingAarsak.EKSISTERENDE_INFOTRYGDSAK)
        assertThat(rutingStatus.rutingAarsak()).isEqualTo(RutingAarsak.FINNES_IKKE_I_PDL)
    }

    @Test
    fun `ingen rutingårsak når ingen av sjekkene slår til`() {
        assertThat(RutingStatus().rutingAarsak()).isNull()
    }

}