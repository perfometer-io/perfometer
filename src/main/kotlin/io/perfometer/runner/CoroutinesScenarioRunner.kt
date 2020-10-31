package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.http.client.HttpClientFactory
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.ScenarioSummary
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

internal class CoroutinesScenarioRunner(
    httpClientFactory: HttpClientFactory,
) : BaseScenarioRunner(httpClientFactory) {

    data class CoroutineHttpClient(
        val httpClient: HttpClient
    ) : AbstractCoroutineContextElement(CoroutineHttpClient) {

        companion object Key : CoroutineContext.Key<CoroutineHttpClient>
    }

    @ExperimentalTime
    override fun runUsers(
        userCount: Int,
        duration: Duration,
        action: suspend () -> Unit,
    ): ScenarioSummary {
        runBlocking(Dispatchers.Default) {
            (1..userCount).map {
                launch(CoroutineHttpClient(httpClientFactory())) {
                    withTimeout(duration.toKotlinDuration()) {
                        while (isActive) action()
                    }
                }
            }
        }
        return statistics.finish()
    }

    override suspend fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(
                coroutineContext[CoroutineHttpClient.Key]?.httpClient ?: throw IllegalStateException(),
                step,
            )
            is PauseStep -> pauseFor(step.duration)
        }
    }

    private suspend fun pauseFor(duration: Duration) {
        delay(duration.toMillis())
        statistics.gather(PauseStatistics(duration))
    }
}
