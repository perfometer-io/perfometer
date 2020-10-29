package io.perfometer.fixture

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import java.time.Duration
import java.time.Instant

internal object ScenarioStatisticsFixture {

    fun singleGetRequestStatistics(): ScenarioStatistics {
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        return scenarioStatistics.gather(
            RequestStatistics(
                "name",
                HttpMethod.GET,
                "/",
                Duration.ofSeconds(1),
                HttpStatus(200)
            )
        )
    }

}
