package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.services.RutingService.RutingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RutingStatusTest {

    @Test
    fun `resultatSomTekst skal gi riktig tekst`() {
        val prefix = "Rutingstatus:"
        val postfixGosys = "=> ${RutingService.Rute.GOSYS_OG_INFOTRYGD}"

        assertThat(RutingStatus(finnesIkkeIPdl = true).resultatSomTekst())
            .isEqualTo("$prefix Personen finnes ikke i PDL $postfixGosys")
        assertThat(RutingStatus(doed = true).resultatSomTekst())
            .isEqualTo("$prefix Personen er død $postfixGosys")
        assertThat(RutingStatus(kode7Fortrolig = true).resultatSomTekst())
            .isEqualTo("$prefix Personen er kode 7 - fortrolig $postfixGosys")
        assertThat(RutingStatus(kode6StrengtFortrolig = true).resultatSomTekst())
            .isEqualTo("$prefix Personen er kode 6 - strengt fortrolig $postfixGosys")
        assertThat(RutingStatus(egenAnsatt = true).resultatSomTekst())
            .isEqualTo("$prefix Personen er egen ansatt/skjermet $postfixGosys")
        assertThat(RutingStatus(aapenGenerellYrkesskadeSak = true).resultatSomTekst())
            .isEqualTo("$prefix Personen har en åpen generell YRK-sak $postfixGosys")
        assertThat(RutingStatus(eksisterendeInfotrygdSak = true).resultatSomTekst())
            .isEqualTo("$prefix Personen har en eksisterende Infotrygd-sak $postfixGosys")
        assertThat(RutingStatus(potensiellKommendeSak = true).resultatSomTekst())
            .isEqualTo("$prefix Personen har en potensiell kommende sak $postfixGosys")
        assertThat(RutingStatus().resultatSomTekst())
            .isEqualTo("$prefix Ingen av sjekkene har slått til, bruk default ruting $postfixGosys")
    }

}