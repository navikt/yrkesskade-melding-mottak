package no.nav.yrkesskade.ysmeldingmottak.controllers

import no.nav.yrkesskade.ysmeldingmottak.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingmottak.services.SkademeldingService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
        path = ["/api/"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
)
class SkademeldingController(private val skademeldingService: SkademeldingService) {

    @PostMapping("/skademelding")
    fun mottaSkademelding(@RequestBody(required = true) skademeldingDto: SkademeldingDto): ResponseEntity<SkademeldingDto> {
        return ResponseEntity.ok().body(skademeldingService.mottaSkademelding(skademeldingDto))
    }
}