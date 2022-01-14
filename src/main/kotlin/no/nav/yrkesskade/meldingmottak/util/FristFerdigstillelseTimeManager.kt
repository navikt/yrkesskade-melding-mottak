package no.nav.yrkesskade.meldingmottak.util

import no.bekk.bekkopen.date.NorwegianDateUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Hentet fra prosjektet jfr-manuell og noe refaktorert.
 * Hjelpeklasse som beregner frist for behandling av en oppgave ut fra en gitt dato.
 * Hvis tidspunkt for den gitte datoen før klokken 12, er fristen neste virkedag.
 * Hvis tidspunkt for den gitte datoen etter klokken 12, er fristen om to virkedager.
 *
 * Bruker biblioteket [NorwegianDateUtil] som tar hensyn til norske fri-/helligdager.
 */
object FristFerdigstillelseTimeManager {
    private const val MIDT_PAA_DAGEN = 12

    fun nesteGyldigeFristForFerdigstillelse(localDateTime: LocalDateTime): LocalDate {
        val localdateSomDate = Date.from(
            localDateTime.toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

        val frist: Date = NorwegianDateUtil.addWorkingDaysToDate(
            localdateSomDate,
            antallDagerTilFristForFerdigstillelse(localDateTime.hour)
        )
        return frist.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun antallDagerTilFristForFerdigstillelse(time: Int): Int {
        return if (time < MIDT_PAA_DAGEN) 1 else 2
    }
}