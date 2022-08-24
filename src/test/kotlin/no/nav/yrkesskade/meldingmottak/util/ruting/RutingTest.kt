package no.nav.yrkesskade.meldingmottak.util.ruting

import io.mockk.mockkObject
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkademelding
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingSykdom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RutingTest {

    @Test
    fun `yrkessykdom blir sendt til yrkessykdomsruting`() {
        mockkObject(YrkessykdomRuting.Companion) {
            Ruting.utledEnhet(skademeldingSykdom())
            verify(exactly = 1) { YrkessykdomRuting.utledEnhet(any()) }
        }
    }

    @Test
    fun `yrkesskade faar enhet null`() {
        mockkObject(YrkessykdomRuting.Companion) {
            val enhet = Ruting.utledEnhet(enkelSkademelding())
            verify(exactly = 0) { YrkessykdomRuting.utledEnhet(any()) }
            assertThat(enhet).isNull()
        }
    }
}