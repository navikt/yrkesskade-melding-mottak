package no.nav.yrkesskade.meldingmottak.util

import no.bekk.bekkopen.date.NorwegianDateUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object FristFerdigstillelseTimeManager {
    private const val SISTE_ARBEIDSTIME = 12

    fun nextValidFristFerdigstillelse(): LocalDate {
        val localDateTime: LocalDateTime = LocalDateTime.now()
        val frist: Date = NorwegianDateUtil.addWorkingDaysToDate(
            Date.from(
                localDateTime.toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            ),
            numberOfDaysUntilFristFerdigstillelse(localDateTime.hour)
        )
        return frist.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun numberOfDaysUntilFristFerdigstillelse(time: Int): Int {
        return if (time < SISTE_ARBEIDSTIME) 1 else 2
    }
}