package no.nav.yrkesskade.meldingmottak.domene

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
internal class NavnTest {

    @Test
    fun `skal skrive ut fullt navn`() {
        val fulltNavn = Navn("Fornavn", "Mellomnavn", "Etternavn")
        assertThat(fulltNavn.toString()).isEqualTo("Fornavn Mellomnavn Etternavn")
    }

    @Test
    fun `skal skrive ut fornavn og etternavn når ingen mellomnavn`() {
        val utenMellomnavn = Navn("Fornavn", null, "Etternavn")
        assertThat(utenMellomnavn.toString()).isEqualTo("Fornavn Etternavn")
    }

    @Test
    fun `skal skrive ut tom streng når navn er null`() {
        val navn: Navn? = null
        assertThat(navn?.toString().orEmpty()).isEqualTo("")
    }

}

