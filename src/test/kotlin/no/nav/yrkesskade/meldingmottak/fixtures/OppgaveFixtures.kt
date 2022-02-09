package no.nav.yrkesskade.meldingmottak.fixtures

import no.nav.yrkesskade.meldingmottak.clients.gosys.Oppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.Prioritet
import no.nav.yrkesskade.meldingmottak.clients.gosys.Status
import java.time.LocalDate
import java.time.OffsetDateTime

fun enkelOppgave(): Oppgave {
    return Oppgave(
        id = 1,
        tildeltEnhetsnr = "4849",
        tema = "YRK",
        oppgavetype = "JFR",
        versjon = 1,
        opprettetAv = "yrkesskade-melding-mottak",
        prioritet = Prioritet.NORM,
        status = Status.AAPNET,
        fristFerdigstillelse = LocalDate.now(),
        aktivDato = LocalDate.now(),
        opprettetTidspunkt = OffsetDateTime.now()
    )
}
