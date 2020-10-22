package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant

internal class CoroutinesScenarioRunner(
        private val httpClient: HttpClient,
        private val scenarioStatistics: ScenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
) : ScenarioRunner {

    override fun runUsers(userCount: Int, block: () -> Unit) {
        runBlocking {
            val jobs: List<Job> = (1..userCount).map {
                launch {
                    block()
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
        scenarioStatistics.gather(PauseStatistics(duration))
    }

    private fun executeHttp(requestStep: RequestStep) {
        val startTime = Instant.now()
        val request = requestStep.request
        val response = httpClient.executeHttp(request)
        val timeElapsed = Duration.between(startTime, Instant.now())
        request.consumer(response)
        scenarioStatistics.gather(RequestStatistics(
                request.method,
                request.pathWithParams,
                timeElapsed,
                response.status)
        )
    }

    override fun statistics(): ScenarioStatistics {
        return scenarioStatistics
    }
}
