package no.nav.yrkesskade.ysmeldingmottak.repositories

import no.nav.yrkesskade.ysmeldingmottak.domain.Skademelding
import org.springframework.data.jpa.repository.JpaRepository

interface SkademeldingRepository: JpaRepository<Skademelding, Long> {
}