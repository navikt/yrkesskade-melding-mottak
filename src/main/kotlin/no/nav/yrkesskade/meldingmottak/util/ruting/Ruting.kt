package no.nav.yrkesskade.meldingmottak.util.ruting

import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.Tidstype

class Ruting {
    companion object {
        fun utledEnhet(skademelding: Skademelding): String? {
            return when (skademelding.hendelsesfakta.tid.tidstype) {
                Tidstype.periode -> YrkessykdomRuting.utledEnhet(skademelding)
                else -> null
            }
        }
    }
}