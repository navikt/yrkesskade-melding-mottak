package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringMetadata
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.Spraak
import no.nav.yrkesskade.skadeforklaring.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset

fun enkelSkadeforklaringInnsendingHendelse(): SkadeforklaringInnsendingHendelse =
    SkadeforklaringInnsendingHendelse(
        metadata = SkadeforklaringMetadata(
            tidspunktMottatt = LocalDateTime.of(2022, Month.APRIL, 8, 17, 30).toInstant(ZoneOffset.UTC),
            spraak = Spraak.NB,
            navCallId = "1234-5678-90"
        ),
        skadeforklaring = enkelSkadeforklaring()
    )

fun enkelSkadeforklaringInnsendingHendelseMedVedlegg(): SkadeforklaringInnsendingHendelse =
    SkadeforklaringInnsendingHendelse(
        metadata = SkadeforklaringMetadata(
            tidspunktMottatt = LocalDateTime.of(2022, Month.APRIL, 8, 17, 30).toInstant(ZoneOffset.UTC),
            spraak = Spraak.NB,
            navCallId = "1234-5678-90"
        ),
        skadeforklaring = enkelSkadeforklaringMedVedlegg()
    )

fun enkelSkadeforklaringInnsendingHendelseMedBildevedlegg(): SkadeforklaringInnsendingHendelse =
    SkadeforklaringInnsendingHendelse(
        metadata = SkadeforklaringMetadata(
            tidspunktMottatt = LocalDateTime.of(2022, Month.APRIL, 8, 17, 30).toInstant(ZoneOffset.UTC),
            spraak = Spraak.NB,
            navCallId = "1234-5678-90"
        ),
        skadeforklaring = enkelSkadeforklaringMedBildevedlegg()
    )

fun enkelSkadeforklaringInnsendingHendelseHvorSkadelidtMelderSelv(): SkadeforklaringInnsendingHendelse =
    SkadeforklaringInnsendingHendelse(
        metadata = SkadeforklaringMetadata(
            tidspunktMottatt = Instant.now(),
            spraak = Spraak.NB,
            navCallId = "2222-3333-4444"
        ),
        skadeforklaring = enkelSkadeforklaringHvorSkadelidtMelderSelv()
    )

fun enkelSkadeforklaring(): Skadeforklaring =
    Skadeforklaring(
        saksnummer = null,
        innmelder = skadeforklaringInnmelderErForesatt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidetMedIUlykkesoeyeblikket = "Dette er arbeidsbeskrivelsen",
        noeyaktigBeskrivelseAvHendelsen = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        skalEttersendeDokumentasjon = "ja",
        vedleggreferanser = emptyList(),
        fravaer = Fravaer("treDagerEllerMindre", "sykemelding"),
        helseinstitusjon = helseinstitusjon()
    )

fun enkelSkadeforklaringMedVedlegg(): Skadeforklaring =
    Skadeforklaring(
        saksnummer = null,
        innmelder = skadeforklaringInnmelderErForesatt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidetMedIUlykkesoeyeblikket = "Dette er arbeidsbeskrivelsen",
        noeyaktigBeskrivelseAvHendelsen = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        skalEttersendeDokumentasjon = "nei",
        vedleggreferanser = vedleggReferanser(),
        fravaer = Fravaer("treDagerEllerMindre", "sykemelding"),
        helseinstitusjon = helseinstitusjon()
    )

fun enkelSkadeforklaringMedBildevedlegg(): Skadeforklaring =
    Skadeforklaring(
        saksnummer = null,
        innmelder = skadeforklaringInnmelderErForesatt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidetMedIUlykkesoeyeblikket = "Dette er arbeidsbeskrivelsen",
        noeyaktigBeskrivelseAvHendelsen = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        skalEttersendeDokumentasjon = "nei",
        vedleggreferanser = vedleggReferanserMedBildevedlegg(),
        fravaer = Fravaer("treDagerEllerMindre", "sykemelding"),
        helseinstitusjon = helseinstitusjon()
    )

fun enkelSkadeforklaringHvorSkadelidtMelderSelv(): Skadeforklaring =
    Skadeforklaring(
        saksnummer = null,
        innmelder = skadeforklaringInnmelderErSkadelidt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidetMedIUlykkesoeyeblikket = "Dette er arbeidsbeskrivelsen",
        noeyaktigBeskrivelseAvHendelsen = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        skalEttersendeDokumentasjon = "ja",
        vedleggreferanser = emptyList(),
        fravaer = Fravaer("treDagerEllerMindre", "egenmelding"),
        helseinstitusjon = helseinstitusjon()
    )

fun skadeforklaringInnmelderErForesatt(): Innmelder =
    Innmelder(
        norskIdentitetsnummer = "12345600000",
        innmelderrolle = "vergeOgForesatt"
    )

fun skadeforklaringInnmelderErSkadelidt(): Innmelder =
    Innmelder(
        norskIdentitetsnummer = "31033355555",
        innmelderrolle = "denSkadelidte"
    )

fun skadeforklaringSkadelidt(): Skadelidt = Skadelidt("12120522222")

fun helseinstitusjon(): Helseinstitusjon =
    Helseinstitusjon(
        erHelsepersonellOppsokt = "ja",
        navn = "Bli-bra-igjen Legesenter",
        adresse = Adresse(
            adresse = "Stien 3B",
            postnummer = "1739",
            poststed = "Granlia"
        )
    )

fun vedleggReferanser(): List<Vedleggreferanse> {
    val vedleggreferanse1 = Vedleggreferanse("vedlegg-1", "Vedlegg1.pdf", 512, "https://vedlegglager/vedlegg1")
    val vedleggreferanse2 = Vedleggreferanse("vedlegg-2", "Vedlegg2.pdf", 300, "https://vedlegglager/vedlegg2")
    return listOf(vedleggreferanse1, vedleggreferanse2)
}

fun vedleggReferanserMedBildevedlegg(): List<Vedleggreferanse> {
    val vedleggreferanse = Vedleggreferanse("vedlegg-9", "katt.jpeg", 1000, "https://ett-vedlegglager/vedlegg9")
    return listOf(vedleggreferanse)
}

