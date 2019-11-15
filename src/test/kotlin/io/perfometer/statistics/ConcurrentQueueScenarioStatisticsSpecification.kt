package io.perfometer.statistics

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

@Suppress("FunctionName")
class ConcurrentQueueScenarioStatisticsSpecification {
    private val startTime = ZonedDateTime.of(1410, 7, 15, 12, 0, 0, 0, ZoneId.of("Europe/Warsaw"))

    @Test
    fun `gather should add statistics to the summary`() {
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(startTime.toInstant())

        val getStatistics = RequestStatistics(HttpMethod.GET, "/", Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(getStatistics)

        val postStatistics = RequestStatistics(HttpMethod.POST, "/", Duration.ofSeconds(1), HttpStatus(201))
        scenarioStatistics.gather(postStatistics)

        val scenarioSummary = scenarioStatistics.finish()
        scenarioSummary.statistics.size shouldBe 2
        scenarioSummary.statistics shouldContain getStatistics
        scenarioSummary.statistics shouldContain postStatistics
    }

    @Test(IllegalStateException::class)
    fun `gather should not allow adding new statistics when the scenario has ended`() {
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(startTime.toInstant())

        val getStatistics = RequestStatistics(HttpMethod.GET, "/", Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(getStatistics)

        scenarioStatistics.finish()
        scenarioStatistics.gather(getStatistics)
    }
}
