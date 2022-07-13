package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandling
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingHendelse
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingMetadata
import java.util.UUID

fun dokumentTilSaksbehandlingHendelse(): DokumentTilSaksbehandlingHendelse =
    DokumentTilSaksbehandlingHendelse(
        DokumentTilSaksbehandling(
            "1337",
            "9999",
        ),
        DokumentTilSaksbehandlingMetadata(UUID.randomUUID().toString())
    )