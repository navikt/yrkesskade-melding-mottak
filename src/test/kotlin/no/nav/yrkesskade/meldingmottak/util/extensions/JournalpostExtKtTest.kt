package no.nav.yrkesskade.meldingmottak.util.extensions

import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import no.nav.yrkesskade.meldingmottak.fixtures.gyldigJournalpostMedAktoerId
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_YRKESSYKDOM
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

//    @Test
//    fun `journalfoerendeEnhetEllerNull gir yrkessykdomsenheten naar brevkoden er arbeidstilsynsmeldingskopi`() {
//        val journalpostMedArbeidstilsynsmeldingKopi = gyldigJournalpostMedAktoerId().copy(
//            dokumenter = listOf(
//                DokumentInfo(
//                    "Kopi av melding om arbeidsrelatert sykdom til Arbeidstilsynet",
//                    Brevkode.ARBEIDSTILSYNSMELDING_KOPI.kode
//                )
//            )
//        )
//
//        assertThat(
//            journalpostMedArbeidstilsynsmeldingKopi.journalfoerendeEnhetEllerNull()
//        ).isEqualTo(ENHET_YRKESSYKDOM)
//    }
}