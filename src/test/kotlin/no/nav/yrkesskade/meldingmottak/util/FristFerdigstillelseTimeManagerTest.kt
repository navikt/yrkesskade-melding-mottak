package no.nav.yrkesskade.meldingmottak.util

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class FristFerdigstillelseTimeManagerTest {

    @Test
    fun `neste gyldige frist er neste dag om klokken er 11 paa en vanlig arbeidsdag`() {
        val klokkenElleveVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 11, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenElleveVanligArbeidsdag)

        val dagenEtter = LocalDate.of(2022, 1, 13)
        assertThat(utregnetFrist).isEqualTo(dagenEtter)
    }

    @Test
    fun `neste gyldige frist er om to dager om klokken er 12 paa en vanlig arbeidsdag`() {
        val klokkenTolvVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 12, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTolvVanligArbeidsdag)

        val toDagerEtter = LocalDate.of(2022, 1, 14)
        assertThat(utregnetFrist).isEqualTo(toDagerEtter)
    }

    @Test
    fun `neste gyldige frist er om to dager om klokken er 13 paa en vanlig arbeidsdag`() {
        val klokkenTrettenVanligArbeidsdag = LocalDateTime.of(2022, 1, 12, 13, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenVanligArbeidsdag)

        val toDagerEtter = LocalDate.of(2022, 1, 14)
        assertThat(utregnetFrist).isEqualTo(toDagerEtter)
    }

    @Test
    fun `neste gyldige frist er om tre dager om klokken er 11 paa en fredag`() {
        val klokkenElleveFredag = LocalDateTime.of(2022, 1, 14, 11, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenElleveFredag)

        val mandagenEtter = LocalDate.of(2022, 1, 17)
        assertThat(utregnetFrist).isEqualTo(mandagenEtter)
    }

    @Test
    fun `neste gyldige frist er om fire dager om klokken er 12 paa en fredag`() {
        val klokkenTrettenFredag = LocalDateTime.of(2022, 1, 14, 12, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenFredag)

        val tirsdagenEtter = LocalDate.of(2022, 1, 18)
        assertThat(utregnetFrist).isEqualTo(tirsdagenEtter)
    }

    @Test
    fun `neste gyldige frist er om fire dager om klokken er 13 paa en fredag`() {
        val klokkenTrettenFredag = LocalDateTime.of(2022, 1, 14, 13, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenFredag)

        val tirsdagenEtter = LocalDate.of(2022, 1, 18)
        assertThat(utregnetFrist).isEqualTo(tirsdagenEtter)
    }

    @Test
    fun `neste gyldige frist er om fire dager om klokken er 13 paa en torsdag`() {
        val klokkenTrettenTorsdag = LocalDateTime.of(2022, 1, 13, 13, 0, 0)
        val utregnetFrist = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(klokkenTrettenTorsdag)

        val mandagenEtter = LocalDate.of(2022, 1, 17)
        assertThat(utregnetFrist).isEqualTo(mandagenEtter)
    }
}