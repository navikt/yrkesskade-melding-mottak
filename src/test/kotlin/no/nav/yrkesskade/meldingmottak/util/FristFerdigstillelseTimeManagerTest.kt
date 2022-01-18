package no.nav.yrkesskade.meldingmottak.util

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class FristFerdigstillelseTimeManagerTest {

    @Test
    fun `neste gyldige frist er neste dag om klokken er 11 paa en vanlig arbeidsdag`() {
        val klokkenElleveVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 11, 0, 0)
        val resultat = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenElleveVanligArbeidsdag)

        val forventetFrist = leggTilDager(klokkenElleveVanligArbeidsdag, 1)
        assertThat(resultat).isEqualTo(forventetFrist)
    }

    @Test
    fun `neste gyldige frist er om to dager om klokken er 12 paa en vanlig arbeidsdag`() {
        val klokkenTolvVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 12, 0, 0)
        val resultat = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTolvVanligArbeidsdag)

        val forventetFrist = leggTilDager(klokkenTolvVanligArbeidsdag, 2)
        assertThat(resultat).isEqualTo(forventetFrist)
    }

    @Test
    fun `neste gyldige frist er om to dager om klokken er 13 paa en vanlig arbeidsdag`() {
        val klokkenTrettenVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 13, 0, 0)
        val resultat = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenVanligArbeidsdag)

        val forventetFrist = leggTilDager(klokkenTrettenVanligArbeidsdag, 2)
        assertThat(resultat).isEqualTo(forventetFrist)
    }

    @Test
    fun `neste gyldige frist er om tre dager om klokken er 11 paa en fredag`() {
        val klokkenElleveFredag = LocalDateTime.of(2022, 1, 14, 11, 0, 0)
        val resultat = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenElleveFredag)

        val forventetFrist = leggTilDager(klokkenElleveFredag, 3)
        assertThat(resultat).isEqualTo(forventetFrist)
    }

    @Test
    fun `neste gyldige frist er om fire dager om klokken er 13 paa en fredag`() {
        val klokkenTrettenFredag = LocalDateTime.of(2022, 1, 14, 13, 0, 0)
        val resultat = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenFredag)

        val forventetFrist = leggTilDager(klokkenTrettenFredag, 4)
        assertThat(resultat).isEqualTo(forventetFrist)
    }

    private fun leggTilDager(localDateTime: LocalDateTime, antallDager: Long): LocalDate {
        return localDateTime.plusDays(antallDager)
            .toLocalDate()
            .atStartOfDay()
            .toLocalDate()
    }
}