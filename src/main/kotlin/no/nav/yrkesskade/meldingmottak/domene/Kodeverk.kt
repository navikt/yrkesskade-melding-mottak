package no.nav.yrkesskade.meldingmottak.domene

import java.time.Instant

typealias KodeverkType = String

typealias KodeverkKategori = String

data class KodeverkTypeKategori(
    val type: KodeverkType,
    val kategori: KodeverkKategori
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
    val data: Map<KodeverkKode, KodeverkVerdi>,
    val hentetTid: Instant = Instant.now()
)

