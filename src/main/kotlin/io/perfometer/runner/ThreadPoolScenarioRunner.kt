package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.PauseStatistics
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class ThreadPoolScenarioRunner(
        httpClient: HttpClient,
) : BaseScenarioRunner(httpClient) {

    override fun runUsers(userCount: Int, action: () -> Unit) {
        val scenarioExecutor = Executors.newFixedThreadPool(userCount)
        CompletableFuture.allOf(
                *(0 until userCount)
                        .map { CompletableFuture.runAsync(action, scenarioExecutor) }
                        .toTypedArray())
                .join()
        shutdown(scenarioExecutor)
    }

    override fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(step)
            is PauseStep -> pauseFor(step.duration)
        }
    }

    private fun shutdown(scenarioExecutor: ExecutorService) {
        scenarioExecutor.shutdown()
        val isTerminated = scenarioExecutor.awaitTermination(10, TimeUnit.SECONDS)
        if (!isTerminated) {
            scenarioExecutor.shutdownNow()
        }
    }

    private fun pauseFor(duration: Duration) {
        Thread.sleep(duration.toMillis())
        statistics.gather(PauseStatistics(duration))
    }
}
