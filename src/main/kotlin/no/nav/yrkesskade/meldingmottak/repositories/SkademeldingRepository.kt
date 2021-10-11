package no.nav.yrkesskade.meldingmottak.repositories

import no.nav.yrkesskade.meldingmottak.domain.Skademelding
import org.springframework.data.jpa.repository.JpaRepository

interface SkademeldingRepository: JpaRepository<Skademelding, Long> {
}