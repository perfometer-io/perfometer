package io.perfometer.statistics

import io.kotlintest.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class ScenarioSummarySpecification {
    private val start = Instant.MIN
    private val end = Instant.MAX

    private val ok = HttpStatus(200)

    @Test
    fun `should return the slowest request`() {
        val fastest = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(3), ok)
        val average = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(2), ok)
        val stats = setOf(fastest, slowest, average)

        ScenarioSummary(stats, start, end).slowestRequest shouldBe slowest
    }

    @Test
    fun `should return the fastest request`() {
        val fastest = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(3), ok)
        val average = RequestStatistics(HttpMethod.GET, "", Duration.ofSeconds(2), ok)

        val stats = setOf(fastest, slowest, average)
        ScenarioSummary(stats, start, end).fastestRequest shouldBe fastest
    }

    @Test
    fun `should return mean average request time`() {
        // given three request taking 1500 ms
        val fastest = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(100), ok)
        val slowest = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(1000), ok)
        val somewhereInBetween = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(400), ok)

        val stats = setOf(fastest, slowest, somewhereInBetween)
        ScenarioSummary(stats, start, end).meanAverageRequestTime shouldBe Duration.ofMillis(500)

        // given three requests summing up to one second
        val hundred = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(100), ok)
        val fiveHundred = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(500), ok)
        val fourHundred = RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(400), ok)

        val oneSecondStats = setOf(hundred, fiveHundred, fourHundred)

        // Should truncate mean average duration to millis
        ScenarioSummary(oneSecondStats, start, end).meanAverageRequestTime shouldBe Duration.ofMillis(333)
    }

    @Test
    fun `should return zero duration if no requests in statistics`() {
        ScenarioSummary(emptyList(), start, end).meanAverageRequestTime shouldBe Duration.ZERO
    }

    @Test
    fun `should return scenario running time`() {
        val summary = ScenarioSummary(setOf(RequestStatistics(HttpMethod.GET, "", Duration.ofMillis(100), ok)),
                start,
                end)
        summary.scenarioTime shouldBe Duration.between(start, end)
    }

    @Test
    fun `should return false if none request stats given`() {
        val emptySummary = ScenarioSummary(emptyList(), start, end)
        val onlyPausesSummary = ScenarioSummary(setOf(PauseStatistics(Duration.ZERO)), start, end)

        emptySummary.hasRequests() shouldBe false
        onlyPausesSummary.hasRequests() shouldBe false
    }

    @Test
    fun `should return true if at least on request stat is present`() {
        ScenarioSummary(setOf(RequestStatistics(HttpMethod.GET, "", Duration.ZERO, ok)), start, end)
                .hasRequests() shouldBe true
    }
}
