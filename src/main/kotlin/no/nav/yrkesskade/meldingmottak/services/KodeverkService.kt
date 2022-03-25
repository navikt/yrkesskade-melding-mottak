package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.clients.Kodeverkklient
import no.nav.yrkesskade.meldingmottak.domene.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class KodeverkService(
    private val kodeverkklient: Kodeverkklient,
    @Value("\${kodeverk.cache.gyldigTidMinutter}") val gyldigTidMinutter: Long = 60
) {
    val kodeverkMap: MutableMap<KodeverkTypeKategori, KodeverkTidData> = mutableMapOf()


    fun hentKodeverk(type: String, kategori: String, spraak: String = "nb"): Map<KodeverkKode, KodeverkVerdi> {
        val key = KodeverkTypeKategori(type, kategori)

        if (!gyldig(type, kategori, spraak)) {
            val map = kodeverkklient.hentKodeverk(type, kategori, spraak)
            kodeverkMap[key] = KodeverkTidData(map)
        }

        return kodeverkMap[key]?.data ?: emptyMap()
    }


    private fun gyldig(type: KodeverkType, kategori: KodeverkKategori, spraak: String): Boolean {
        val key = KodeverkTypeKategori(type, kategori)
        val kodeverkTidData: KodeverkTidData? = kodeverkMap[key]

        if (kodeverkTidData == null ||
            kodeverkTidData.hentetTid.isBefore(Instant.now().plus(-gyldigTidMinutter, ChronoUnit.MINUTES))
        ) {
            return false
        }
        return true
    }
}