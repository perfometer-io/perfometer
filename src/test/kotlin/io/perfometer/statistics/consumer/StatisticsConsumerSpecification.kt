package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.StatisticsFixture
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.Output.HTML
import io.perfometer.statistics.consumer.Output.TEXT_FILE
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
        val scenarioSummary: ScenarioSummary = StatisticsFixture.singleGetRequestScenarioSummary()

        // when consuming the stats with Output.TEXT_FILE option selected
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
        val scenarioSummary: ScenarioSummary = StatisticsFixture.singleGetRequestScenarioSummary()

        // when
        consumeStatistics(scenarioSummary, HTML)

        // then
        val timestamp = dateTimeFormatter.format(scenarioSummary.startTime.toZonedDateTimeUTC())
        val reportPath = reportDirectoryPath.resolve("report-${timestamp}${HTML.fileExtension}")
        assertTrue { Files.exists(reportPath) }
        assertTrue { Files.lines(reportPath).count() >= 3 }
    }

    private fun removeReportFiles() {
        if (Files.exists(reportDirectoryPath)) {
            val files = reportDirectoryPath.toFile().listFiles() ?: emptyArray()
            files.forEach { it.delete() }
        }
    }
}
