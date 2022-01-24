package no.nav.yrkesskade.meldingmottak.controllers

import no.nav.security.token.support.core.api.Unprotected
import no.nav.yrkesskade.meldingmottak.models.SkademeldingDto
import no.nav.yrkesskade.meldingmottak.services.SkademeldingService
import no.nav.yrkesskade.meldingmottak.task.DummyTask
import no.nav.yrkesskade.prosessering.domene.Task
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(
        path = ["/api/"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
class SkademeldingController(
    private val skademeldingService: SkademeldingService,
    private val taskRepository: TaskRepository
) {

    @PostMapping("/skademelding", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun mottaSkademelding(@RequestBody(required = true) skademeldingDto: SkademeldingDto): ResponseEntity<SkademeldingDto> {
        return ResponseEntity.ok().body(skademeldingService.mottaSkademelding(skademeldingDto))
    }

    @GetMapping("/skademelding")
    fun hentSkademeldinger(): ResponseEntity<List<SkademeldingDto>> {
        return ResponseEntity.ok().body(skademeldingService.hentAlleSkademeldinger())
    }

    @Unprotected
    @PostMapping("/dummytask")
    fun dummytask(): Iterable<Task> {
        val dummyTask = DummyTask.opprettTask("dette er en payload ${UUID.randomUUID()}")
        taskRepository.save(dummyTask)
        return taskRepository.findAll()
    }
}