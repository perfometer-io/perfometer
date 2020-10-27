package io.perfometer.statistics

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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

        val getStatistics = RequestStatistics("GET /", HttpMethod.GET, "/", Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(getStatistics)

        val postStatistics = RequestStatistics("POST /", HttpMethod.POST, "/", Duration.ofSeconds(1), HttpStatus(201))
        scenarioStatistics.gather(postStatistics)

        val scenarioSummary = scenarioStatistics.finish()
        scenarioSummary.totalSummary.shouldNotBeNull()
                .requestCount shouldBe 2
        scenarioSummary.summaries.size shouldBe 2
    }

    @Test(IllegalStateException::class)
    fun `gather should not allow adding new statistics when the scenario has ended`() {
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(startTime.toInstant())

        val getStatistics = RequestStatistics("GET /", HttpMethod.GET, "/", Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(getStatistics)

        scenarioStatistics.finish()
        scenarioStatistics.gather(getStatistics)
    }
}
