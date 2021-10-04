package no.nav.yrkesskade.ysmeldingmottak.services

import no.nav.yrkesskade.ysmeldingmottak.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingmottak.repositories.SkademeldingRepository
import org.springframework.stereotype.Service

@Service
class SkademeldingService(private val skademeldingRepository: SkademeldingRepository) {

    fun mottaSkademelding(skademeldingDto: SkademeldingDto): SkademeldingDto {
        val lagretSkademelding = skademeldingRepository.save(skademeldingDto.toSkademelding())
        return lagretSkademelding.toSkademeldingDto()
    }
}