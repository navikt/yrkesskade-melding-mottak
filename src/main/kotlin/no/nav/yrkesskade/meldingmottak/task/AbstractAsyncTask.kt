package no.nav.yrkesskade.meldingmottak.task

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.domene.Task
const val TASK_STEP_TYPE = "ProsesserSkadeforklaring"
abstract class AbstractAsyncTask<T> : AsyncTaskStep {

    fun opprettTask(hendelse: T): Task {
        val jacksonObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        return Task(
            type = TASK_STEP_TYPE,
            payload = jacksonObjectMapper.writeValueAsString(
                ProsesserTaskDto(hendelse)
            )
        )
    }
}

data class ProsesserTaskDto<T>(val hendelse: T)

