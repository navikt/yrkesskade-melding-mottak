package no.nav.yrkesskade.meldingmottak.util.kodeverk

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto
import no.nav.yrkesskade.meldingmottak.services.KodeverkService

class KodeverkHolder private constructor(private val kodeverkService: KodeverkService) {

    private val kodeverk: MutableMap<String, Map<String, KodeverdiDto>> = mutableMapOf()

    fun mapKodeTilVerdi(kode: String, kodeliste: String): String {
        return kodeverk.get(kodeliste)!!.getOrDefault(kode, KodeverdiDto(kode, "Ukjent $kode")).verdi!!
    }

    fun hentKodeverk(kategorinavn: String?) {
        // kodeverk uten kategorier
        listOf("rolletype", "landkoder", "fravaertype", "innmelderrolle").forEach {
            kodeverk[it] = kodeverkService.hentKodeverk(it, null, "nb")
        }

        if (kategorinavn != null) {
            // kodeverk med kategorier
            listOf("stillingstittel", "harSkadelidtHattFravaer", "tidsrom", "hvorSkjeddeUlykken", "typeArbeidsplass", "skadetype", "skadetKroppsdel", "bakgrunnForHendelsen", "aarsakOgBakgrunn", "alvorlighetsgrad").forEach {
                kodeverk[it] = kodeverkService.hentKodeverk(it, kategorinavn, "nb")
            }
        }
    }

    companion object {
        fun init (kategorinavn: String? = null, kodeverkService: KodeverkService): KodeverkHolder {
            return KodeverkHolder(kodeverkService).apply {
                hentKodeverk(kategorinavn)
            }
        }
    }
}