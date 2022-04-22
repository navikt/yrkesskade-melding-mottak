package no.nav.yrkesskade.meldingmottak.pdf.domene

import java.time.*
import java.time.format.DateTimeFormatter

object MapperUtil {

    fun datoFormatert(instant: Instant?): String {
        return datoFormatert(toLocalDate(instant))
    }

    fun datoFormatert(dateTime: OffsetDateTime?): String {
        return dateTime?.toLocalDate()?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""
    }

    fun datoFormatert(date: LocalDate?): String {
        return date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""
    }

    fun klokkeslettFormatert(instant: Instant?): String {
        return toLocalDateTime(instant)?.format(DateTimeFormatter.ofPattern("HH.mm")) ?: ""
    }

    fun klokkeslettFormatert(offsetDateTime: OffsetDateTime?): String {
        return offsetDateTime?.atZoneSameInstant(ZoneId.of("Europe/Oslo"))?.toLocalTime()
            ?.format(DateTimeFormatter.ofPattern("HH.mm")) ?: ""
    }

    private fun toLocalDate(instant: Instant?): LocalDate? {
        if (instant == null) {
            return null
        }
        return LocalDate.ofInstant(
            instant,
            ZoneId.of("Europe/Oslo")
        )
    }

    private fun toLocalDateTime(instant: Instant?): LocalDateTime? {
        if (instant == null) {
            return null
        }
        return LocalDateTime.ofInstant(
            instant,
            ZoneId.of("Europe/Oslo")
        )
    }

    fun jaNei(boolean: Boolean): String {
        return when (boolean) {
            true -> "Ja"
            false -> "Nei"
        }
    }

    fun jaNei(string: String): String {
        return when (string.lowercase()) {
            "ja" -> "Ja"
            else -> "Nei"
        }
    }

}
