package no.nav.yrkesskade.meldingmottak.util.ruting

import no.nav.yrkesskade.meldingmottak.services.RutingStatus
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.Tidstype

class Enhetsruting {
    companion object {
        fun utledEnhet(skademelding: Skademelding, rutingStatus: RutingStatus): String? {
            return when (skademelding.hendelsesfakta.tid.tidstype) {
                Tidstype.periode -> YrkessykdomEnhetsruting.utledEnhet(skademelding, rutingStatus)
                else -> null
            }
        }
    }
}