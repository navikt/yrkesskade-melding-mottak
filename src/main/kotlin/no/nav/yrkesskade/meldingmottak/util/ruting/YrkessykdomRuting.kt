package no.nav.yrkesskade.meldingmottak.util.ruting

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_YRKESSYKDOM
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.Tidstype

/**
 * Implementasjon av logikk for utledning av enhet basert på hvilke verdier som er gitt for "skadelig påvirkning".
 * https://confluence.adeo.no/display/MAYYMYFSN/Routing+av+skademeldinger+mellom+yrkesskadeavdelinger+og+yrkessykdomsavdeling
 */
class YrkessykdomRuting {

    companion object {
        private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

        fun utledEnhet(skademelding: Skademelding): String? {
            check(skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode) {
                "Ingen yrkessykdom; tidstype må være periode"
            }
            val yrkessykdomsregler = yamlMapper.readValue(
                this::class.java.classLoader.getResource("ruting/rutingregler.yaml"),
                Rutingfil::class.java
            ).rutingregler.yrkessykdom

            val paavirkningsform = skademelding.hendelsesfakta.paavirkningsform
            return when {
                paavirkningsform.isNullOrEmpty() -> null
                paavirkningsform.any { it in yrkessykdomsregler.regel1.kodeverdier } -> yrkessykdomsregler.regel1.enhet
                paavirkningsform.any { it in yrkessykdomsregler.regel2.kodeverdier } -> yrkessykdomsregler.regel2.enhet
                paavirkningsform.size > 1 -> ENHET_YRKESSYKDOM
                else -> null
            }
        }
    }
}