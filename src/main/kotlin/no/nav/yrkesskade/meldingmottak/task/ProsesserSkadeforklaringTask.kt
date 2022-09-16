package no.nav.yrkesskade.meldingmottak.task

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.yrkesskade.meldingmottak.services.SkadeforklaringService
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import no.nav.yrkesskade.skadeforklaring.integration.mottak.ISkadeforklaringInnsendingHendelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles

@TaskStepBeskrivelse(
    taskStepType = ProsesserSkadeforklaringTask.TASK_STEP_TYPE,
    beskrivelse = "Prosessering av skadeforklaring",
    maxAntallFeil = 10,
    triggerTidVedFeilISekunder = 60 * 30
)
@Component
class ProsesserSkadeforklaringTask(
    val skadeforklaringService: SkadeforklaringService
) : AbstractAsyncTask<ISkadeforklaringInnsendingHendelse>() {

    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()
    val jacksonObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun doTask(task: Task) {
        log.info("Starter ProsesserSkadeforklaringTask")
        secureLogger.info("ProsesserSkadeforklaringTask kjoerer med payload ${task.payload}")

        val payload = jacksonObjectMapper.readValue<ProsesserTaskDto<ISkadeforklaringInnsendingHendelse>>(task.payload)
        skadeforklaringService.mottaSkadeforklaring(payload.hendelse)
        log.info("Hendelse er en ukjent")
    }

    companion object {
        const val TASK_STEP_TYPE = "ProsesserSkadeforklaring"
    }
}
