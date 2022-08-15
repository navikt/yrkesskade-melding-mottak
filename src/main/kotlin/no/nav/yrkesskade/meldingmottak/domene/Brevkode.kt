package no.nav.yrkesskade.meldingmottak.domene

enum class Brevkode(val kode: String) {
    DIGITAL_SKADEFORKLARING("NAV 13-00.21"),
    TANNLEGEERKLAERING("NAV 13-00.08"),
    ARBEIDSTILSYNSMELDING_KOPI("NAV 13-00.50")
}