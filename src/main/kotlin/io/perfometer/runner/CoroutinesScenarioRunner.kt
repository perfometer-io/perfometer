package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.PauseStatistics
import kotlinx.coroutines.*
import java.time.Duration

internal class CoroutinesScenarioRunner(
        httpClient: HttpClient,
) : BaseScenarioRunner(httpClient) {

    override fun runUsers(userCount: Int, action: () -> Unit) {
        runBlocking {
            val jobs: List<Job> = (1..userCount).map {
                launch {
                    action()
                }
            }
            jobs.joinAll()
        }
    }

    override fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(step)
            is PauseStep -> pauseFor(step.duration)
        }
    }

    private fun pauseFor(duration: Duration) {
        runBlocking {
            delay(duration.toMillis())
        }
        statistics.gather(PauseStatistics(duration))
    }
}
