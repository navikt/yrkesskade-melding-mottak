package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.domene.BeriketData
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.integration.model.*
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringMetadata
import no.nav.yrkesskade.meldingmottak.integration.mottak.model.Spraak
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
        skadeforklaring = enkelSkadeforklaring(),
        beriketData = BeriketData(
            innmeldersNavn = Navn("Ola", "B", "Normann"),
            skadelidtsNavn = Navn("Lisa", null, "Normann"),
            skadelidtsBostedsadresse = null
        )
    )

fun enkelSkadeforklaringInnsendingHendelseHvorSkadelidtMelderSelv(): SkadeforklaringInnsendingHendelse =
    SkadeforklaringInnsendingHendelse(
        metadata = SkadeforklaringMetadata(
            tidspunktMottatt = Instant.now(),
            spraak = Spraak.NB,
            navCallId = "2222-3333-4444"
        ),
        skadeforklaring = enkelSkadeforklaringHvorSkadelidtMelderSelv(),
        beriketData = BeriketData(
            innmeldersNavn = Navn("Lisa", null, "Normann"),
            skadelidtsNavn = Navn("Lisa", null, "Normann"),
            skadelidtsBostedsadresse = null
        )
    )

fun enkelSkadeforklaring(): Skadeforklaring =
    Skadeforklaring(
        innmelder = skadeforklaringInnmelderErForesatt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidsbeskrivelse = "Dette er arbeidsbeskrivelsen",
        ulykkesbeskrivelse = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        vedleggtype = "",
        vedleggreferanser = emptyList(),
        fravaer = Fravaer(true, "sykemelding"),
        behandler = behandler()
    )

fun enkelSkadeforklaringHvorSkadelidtMelderSelv(): Skadeforklaring =
    Skadeforklaring(
        innmelder = skadeforklaringInnmelderErSkadelidt(),
        skadelidt = skadeforklaringSkadelidt(),
        arbeidsbeskrivelse = "Dette er arbeidsbeskrivelsen",
        ulykkesbeskrivelse = "Dette er ulykkesbeskrivelsen",
        tid = Tid(
            tidstype = "Tidspunkt",
            tidspunkt = LocalDateTime.of(2022, Month.APRIL, 10, 14, 3, 50).toInstant(ZoneOffset.UTC),
            periode = null
        ),
        vedleggtype = "",
        vedleggreferanser = emptyList(),
        fravaer = Fravaer(true, "egenmelding"),
        behandler = behandler()
    )

fun skadeforklaringInnmelderErForesatt(): Innmelder =
    Innmelder(
        norskIdentitetsnummer = "12345600000",
        rolle = "Foresatt"
    )

fun skadeforklaringInnmelderErSkadelidt(): Innmelder =
    Innmelder(
        norskIdentitetsnummer = "31033355555",
        rolle = "Skadelidt"
    )

fun skadeforklaringSkadelidt(): Skadelidt = Skadelidt("12120522222")

fun behandler(): Behandler =
    Behandler(
        erBehandlerOppsokt = true,
        behandlerNavn = "Bli-bra-igjen Legesenter",
        adresse = Adresse(
            adresse = "Stien 3B",
            postnummer = "1739",
            poststed = "Granlia"
        )
    )
