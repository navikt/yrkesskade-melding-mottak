package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.models.SkademeldingDto
import no.nav.yrkesskade.meldingmottak.repositories.SkademeldingRepository
import org.springframework.stereotype.Service

@Service
class SkademeldingService(private val skademeldingRepository: SkademeldingRepository) {

    fun mottaSkademelding(skademeldingDto: SkademeldingDto): SkademeldingDto {
        val lagretSkademelding = skademeldingRepository.save(skademeldingDto.toSkademelding())
        return lagretSkademelding.toSkademeldingDto()
    }

    fun hentAlleSkademeldinger(): List<SkademeldingDto> {
        return skademeldingRepository.findAll().map { it.toSkademeldingDto() }
    }
}