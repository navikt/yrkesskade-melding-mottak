package no.nav.yrkesskade.meldingmottak.task

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.yrkesskade.meldingmottak.services.SkademeldingService
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles


@TaskStepBeskrivelse(
    taskStepType = ProsesserDigitalSkademeldingTask.TASK_STEP_TYPE,
    beskrivelse = "Prosessering av digital skademelding",
    maxAntallFeil = 10,
    triggerTidVedFeilISekunder = 60 * 30
)
@Component
class ProsesserDigitalSkademeldingTask(
    val skademeldingService: SkademeldingService
) : AsyncTaskStep {

    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()
    val jacksonObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun doTask(task: Task) {
        log.info("Starter ProsesserDigitalSkademeldingTask")
        secureLogger.info("ProsesserDigitalSkademeldingTask kjoerer med payload ${task.payload}")

        val payload = jacksonObjectMapper.readValue<ProsesserDigitalSkademeldingTaskPayloadDto>(task.payload)
        skademeldingService.mottaSkademelding(payload.skademeldingInnsendtHendelse)
        log.info("ProsesserDigitalSkademeldingTask ferdig")
    }

    companion object {
        fun opprettTask(skademeldingInnsendtHendelse: SkademeldingInnsendtHendelse): Task {
            val jacksonObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
            return Task(
                type = TASK_STEP_TYPE,
                payload = jacksonObjectMapper.writeValueAsString(
                    ProsesserDigitalSkademeldingTaskPayloadDto(skademeldingInnsendtHendelse)
                )
            )
        }

        const val TASK_STEP_TYPE = "ProsesserDigitalSkademelding"
    }
}

data class ProsesserDigitalSkademeldingTaskPayloadDto(val skademeldingInnsendtHendelse: SkademeldingInnsendtHendelse)