package no.nav.yrkesskade.meldingmottak.domene

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto
import java.time.Instant

typealias KodeverkType = String

typealias KodeverkKategori = String

data class KodeverkTypeKategori(
    val type: String,
    val kategori: String?
)

typealias KodeverkKode = String

data class KodeverkVerdi(
    val kode: String,
    val verdi: String,
)

data class KodeverdiRespons (
    var kodeverdierMap: Map<KodeverkKode, KodeverkVerdi> = mutableMapOf()
)

data class KodeverkTidData(
    val data: Map<String, KodeverdiDto>,
    val hentetTid: Instant = Instant.now()
)

