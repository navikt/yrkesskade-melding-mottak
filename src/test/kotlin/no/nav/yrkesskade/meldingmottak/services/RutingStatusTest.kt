package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.services.RutingService.RutingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RutingStatusTest {

    @Test
    fun `toString skal gi riktig tekst`() {
        val prefix = "Rutingstatus:"
        val postfixGosys = "=> ${RutingService.Rute.GOSYS_OG_INFOTRYGD}"

        assertThat(RutingStatus(finnesIkkeIPdl = true).toString())
            .isEqualTo("$prefix Personen finnes ikke i PDL $postfixGosys")
        assertThat(RutingStatus(doed = true).toString())
            .isEqualTo("$prefix Personen er død $postfixGosys")
        assertThat(RutingStatus(kode7Fortrolig = true).toString())
            .isEqualTo("$prefix Personen er kode 7 - fortrolig $postfixGosys")
        assertThat(RutingStatus(kode6StrengtFortrolig = true).toString())
            .isEqualTo("$prefix Personen er kode 6 - strengt fortrolig $postfixGosys")
        assertThat(RutingStatus(egenAnsatt = true).toString())
            .isEqualTo("$prefix Personen er egen ansatt/skjermet $postfixGosys")
        assertThat(RutingStatus(aapenGenerellYrkesskadeSak = true).toString())
            .isEqualTo("$prefix Personen har en åpen generell YRK-sak $postfixGosys")
        assertThat(RutingStatus(eksisterendeInfotrygdSak = true).toString())
            .isEqualTo("$prefix Personen har en eksisterende Infotrygd-sak $postfixGosys")
        assertThat(RutingStatus(potensiellKommendeSak = true).toString())
            .isEqualTo("$prefix Personen har en potensiell kommende sak $postfixGosys")
        assertThat(RutingStatus().toString())
            .isEqualTo("$prefix Ingen av sjekkene har slått til, bruk default ruting $postfixGosys")
    }

}