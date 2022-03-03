package no.nav.yrkesskade.meldingmottak.pdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import org.junit.jupiter.api.Test

internal class PdfSkademeldingMapperTest {

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())


    @Test
    fun `skal mappe skademelding til PdfSkademeldig`() {
        val record = skademeldingInnsendtHendelse()
        println("skademeldingen er:\n $record")

        val pdfSkademelding = PdfSkademeldingMapper.tilPdfSkademelding(record)
        println("PdfSkademeldingen er $pdfSkademelding")

        val pdfSkademeldingJson = objectMapper.writeValueAsString(pdfSkademelding)
        println("Og JSON er $pdfSkademeldingJson")

        val prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pdfSkademelding)
        println("Pretty JSON er \n$prettyJson")
    }
}