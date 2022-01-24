package no.nav.yrkesskade.meldingmottak.task

import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import java.time.LocalDateTime
import java.util.Properties

@TaskStepBeskrivelse(
    taskStepType = DummyTask.TASK_STEP_TYPE,
    beskrivelse = "En tulletask",
    maxAntallFeil = 10,
    triggerTidVedFeilISekunder = 60 * 60 * 24
)
@Component
class DummyTask(
    private val taskRepository: TaskRepository,
) : AsyncTaskStep {

    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override fun doTask(task: Task) {
        log.info("Dummytask kjoerer med payload ${task.payload}")

        TODO("Not yet implemented")
    }

    companion object {
        fun opprettTask(payload: String): Task {
            return Task(
                type = TASK_STEP_TYPE,
                payload = payload,
                properties = Properties().apply {
                    this["prop1"] = "testing"
                }
            ).medTriggerTid(
                triggerTid = LocalDateTime.now().plusMinutes(1) // TODO: Settes med liten forsinkelse for test
            ).plukker()
        }

        const val TASK_STEP_TYPE = "dummy"
    }
}