package no.nav.yrkesskade.meldingmottak.util.ruting

import io.mockk.mockkObject
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkademelding
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingSykdom
import no.nav.yrkesskade.meldingmottak.services.RutingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EnhetsrutingTest {

    @Test
    fun `yrkessykdom blir sendt til yrkessykdomsruting`() {
        mockkObject(YrkessykdomEnhetsruting.Companion) {
            Enhetsruting.utledEnhet(skademeldingSykdom(), RutingStatus())
            verify(exactly = 1) { YrkessykdomEnhetsruting.utledEnhet(any(), any()) }
        }
    }

    @Test
    fun `yrkesskade faar enhet null`() {
        mockkObject(YrkessykdomEnhetsruting.Companion) {
            val enhet = Enhetsruting.utledEnhet(enkelSkademelding(), RutingStatus())
            verify(exactly = 0) { YrkessykdomEnhetsruting.utledEnhet(any(), any()) }
            assertThat(enhet).isNull()
        }
    }
}