package no.nav.yrkesskade.meldingmottak.util.ruting

import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkademelding
import no.nav.yrkesskade.meldingmottak.fixtures.hendelsesfaktaSykdom
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingSykdom
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_VIKAFOSSEN
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_YRKESSYKDOM
import no.nav.yrkesskade.meldingmottak.services.RutingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class YrkessykdomEnhetsrutingTest {

    @Test
    fun `skal gi exception naar tidstype ikke er periode`() {
        val exception = assertThrows<IllegalStateException> {
            YrkessykdomEnhetsruting.utledEnhet(enkelSkademelding(), RutingStatus())
        }
        assertThat(exception.localizedMessage).contains("tidstype må være periode")
    }

    @Test
    fun `skal gi tom enhet naar person er kode 6`() {
        val enhet = YrkessykdomEnhetsruting.utledEnhet(skademeldingSykdom(), RutingStatus(kode6StrengtFortrolig = true))
        assertThat(enhet).isEqualTo(ENHET_VIKAFOSSEN)
    }

    @Test
    fun `skal gi tom enhet naar paavirkningsform er tom liste`() {
        val skademeldingSykdom = skademeldingSykdom()
        val skademeldingSykdomUtenPaavirkningsform = skademeldingSykdom.copy(
            hendelsesfakta = hendelsesfaktaSykdom().copy(
                paavirkningsform = emptyList()
            )
        )
        val enhet = YrkessykdomEnhetsruting.utledEnhet(skademeldingSykdomUtenPaavirkningsform, RutingStatus())
        assertThat(enhet).isNull()
    }

    @Test
    fun `skal gi tom enhet naar paavirkningsform er null`() {
        val skademeldingSykdom = skademeldingSykdom()
        val skademeldingSykdomUtenPaavirkningsform = skademeldingSykdom.copy(
            hendelsesfakta = hendelsesfaktaSykdom().copy(
                paavirkningsform = null
            )
        )
        val enhet = YrkessykdomEnhetsruting.utledEnhet(skademeldingSykdomUtenPaavirkningsform, RutingStatus())
        assertThat(enhet).isNull()
    }

    @Test
    fun `regel1 gir enhet yrkessykdom`() {
        val enhet = YrkessykdomEnhetsruting.utledEnhet(skademeldingSykdom(), RutingStatus())
        assertThat(enhet).isEqualTo(ENHET_YRKESSYKDOM)
    }

    @Test
    fun `regel2 gir tom enhet (enhet avgjoeres av NORG)`() {
        val skademeldingSykdom = skademeldingSykdom()
        val skademeldingSykdomSomTrefferRegel2 = skademeldingSykdom.copy(
            hendelsesfakta = hendelsesfaktaSykdom().copy(
                paavirkningsform = listOf(
                    "naturhendelserSomSnoeskredOgJordskred"
                )
            )
        )

        val enhet = YrkessykdomEnhetsruting.utledEnhet(skademeldingSykdomSomTrefferRegel2, RutingStatus())
        assertThat(enhet).isNull()
    }
}