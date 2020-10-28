package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.FileTypeExtension.TXT
import java.io.File
import java.time.ZonedDateTime

internal object StatisticsTextFileWriter : StatisticsFileWriter() {

    fun write(scenarioSummary: ScenarioSummary) {
        val formatted: String = StatisticsTextFormatter.format(scenarioSummary)
        val file: File = createReportFile(scenarioSummary.startTime.toZonedDateTimeUTC())
        file.writeText(formatted)
    }

    private fun createReportFile(startTime: ZonedDateTime): File {
        val reportFile = buildFilePath(startTime, TXT).toFile()
        if (!reportFile.parentFile.exists()) {
            reportFile.parentFile.mkdirs()
        }
        if (!reportFile.exists()) {
            reportFile.createNewFile()
        }
        return reportFile
    }

}
