package no.nav.yrkesskade.meldingmottak.util.ruting

import no.nav.yrkesskade.meldingmottak.fixtures.enkelSkademelding
import no.nav.yrkesskade.meldingmottak.fixtures.hendelsesfaktaSykdom
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingSykdom
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_YRKESSYKDOM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class YrkessykdomRutingTest {

    @Test
    fun `skal gi exception naar tidstype ikke er periode`() {
        val exception = assertThrows<IllegalStateException> {
            YrkessykdomRuting.utledEnhet(enkelSkademelding())
        }
        assertThat(exception.localizedMessage).contains("tidstype må være periode")
    }

    @Test
    fun `skal gi tom enhet naar paavirkningsform er tom liste`() {
        val skademeldingSykdom = skademeldingSykdom()
        val skademeldingSykdomUtenPaavirkningsform = skademeldingSykdom.copy(
            hendelsesfakta = hendelsesfaktaSykdom().copy(
                paavirkningsform = emptyList()
            )
        )
        val enhet = YrkessykdomRuting.utledEnhet(skademeldingSykdomUtenPaavirkningsform)
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
        val enhet = YrkessykdomRuting.utledEnhet(skademeldingSykdomUtenPaavirkningsform)
        assertThat(enhet).isNull()
    }

    @Test
    fun `regel1 gir enhet yrkessykdom`() {
        val enhet = YrkessykdomRuting.utledEnhet(skademeldingSykdom())
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

        val enhet = YrkessykdomRuting.utledEnhet(skademeldingSykdomSomTrefferRegel2)
        assertThat(enhet).isNull()
    }
}