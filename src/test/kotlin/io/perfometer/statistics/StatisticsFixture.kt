package io.perfometer.statistics

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Instant

internal object StatisticsFixture {

    fun singleGetRequestStatistics(
        startTime: Instant = Instant.ofEpochSecond(1),
        endTime: Instant = Instant.ofEpochSecond(2),
        httpStatus: HttpStatus = HttpStatus(200),
    ): RequestStatistics {
        return RequestStatistics(
            "name",
            HttpMethod.GET,
            "/",
            startTime,
            endTime,
            httpStatus
        )
    }

    fun singleGetRequestScenarioSummary(): ScenarioSummary {
        val scenarioStatistics = ConcurrentQueueStatisticsCollector()
        scenarioStatistics.start(Instant.ofEpochSecond(0))
        scenarioStatistics.gather(singleGetRequestStatistics())
        return scenarioStatistics.finish(Instant.ofEpochSecond(5))
    }
}
