package io.perfometer.statistics.consumer

import io.perfometer.fixture.ScenarioStatisticsFixture
import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.Output.*
import io.perfometer.statistics.consumer.StatisticsFileWriter.Companion.dateTimeFormatter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class StatisticsConsumerSpecification {

    private val reportDirectoryPath = Paths.get("${System.getProperty("user.dir")}/reports")

    @BeforeTest
    fun setup() {
        removeReportFiles()
    }

    @AfterTest
    fun cleanup() {
        removeReportFiles()
    }

    @Test
    fun `should create a txt report file`() {
        // given a statistics with summary
        val scenarioStatistics = ScenarioStatisticsFixture.singleGetRequestStatistics()

        // when consuming the stats with Output.TEXT_FILE option selected
        val scenarioSummary: ScenarioSummary = scenarioStatistics.finish()
        consumeStatistics(scenarioSummary, TEXT_FILE)

        // then a text file should be created with scenario start time UTC timestamp in filename
        val timestamp = dateTimeFormatter.format(scenarioSummary.startTime.toZonedDateTimeUTC())
        val reportPath =
            reportDirectoryPath.resolve("report-${timestamp}${TEXT_FILE.fileExtension}")
        assertTrue { Files.exists(reportPath) }
        assertTrue { Files.lines(reportPath).count() >= 3 }
    }

    @Test
    fun `should create an HTML report file`() {
        // given
        val scenarioStatistics = ScenarioStatisticsFixture.singleGetRequestStatistics()

        // when
        val scenarioSummary: ScenarioSummary = scenarioStatistics.finish()
        consumeStatistics(scenarioSummary, HTML)

        // then
        val timestamp = dateTimeFormatter.format(scenarioSummary.startTime.toZonedDateTimeUTC())
        val reportPath = reportDirectoryPath.resolve("report-${timestamp}${HTML.fileExtension}")
        assertTrue { Files.exists(reportPath) }
        assertTrue { Files.lines(reportPath).count() >= 3 }
    }

    @Test
    fun `should create PDF report file`() {
        // given
        val scenarioStatistics = ScenarioStatisticsFixture.singleGetRequestStatistics()

        // when
        val scenarioSummary: ScenarioSummary = scenarioStatistics.finish()
        consumeStatistics(scenarioSummary, PDF)

        // then
        val timestamp = dateTimeFormatter.format(scenarioSummary.startTime.toZonedDateTimeUTC())
        val reportPath = reportDirectoryPath.resolve("report-${timestamp}${PDF.fileExtension}")
        assertTrue { Files.exists(reportPath) }
    }

    private fun removeReportFiles() {
        if (Files.exists(reportDirectoryPath)) {
            val files = reportDirectoryPath.toFile().listFiles() ?: emptyArray()
            files.forEach { it.delete() }
        }
    }
}
