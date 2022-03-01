package no.nav.yrkesskade.meldingmottak.util.extensions

import no.nav.yrkesskade.meldingmottak.fixtures.gyldigJournalpostMedAktoerId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JournalpostExtKtTest {

    @Test
    fun `journalfoerendeEnhetEllerNull gir null naar journalfoerendeEnhet er null`() {
        val journalpostMedNullSomEnhet = gyldigJournalpostMedAktoerId().copy(
            journalfoerendeEnhet = null
        )
        assertThat(journalpostMedNullSomEnhet.journalfoerendeEnhetEllerNull()).isNull()
    }

    @Test
    fun `journalfoerendeEnhetEllerNull gir 4849 naar journalfoerendeEnhet er 4849`() {
        val journalpost = gyldigJournalpostMedAktoerId()
        assertThat(journalpost.journalfoerendeEnhetEllerNull()).isEqualTo(journalpost.journalfoerendeEnhet)
    }

    @Test
    fun `journalfoerendeEnhetEllerNull gir null naar journalfoerendeEnhet er nedlagt`() {
        val journalpostMedNedlagtEnhet = gyldigJournalpostMedAktoerId().copy(
            journalfoerendeEnhet = "0889"
        )
        assertThat(journalpostMedNedlagtEnhet.journalfoerendeEnhetEllerNull()).isNull()
    }
}