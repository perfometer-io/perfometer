package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

internal object StatisticsTextFileWriter {

    fun write(scenarioSummary: ScenarioSummary) {
        val formatted: String = StatisticsTextFormatter.format(scenarioSummary)
        val file: File = createReportFile()
        Files.write(file.toPath(), formatted.toByteArray(Charset.forName("UTF-8")))
    }

    private fun createReportFile(): File {
        val file = Paths.get("${System.getProperty("user.dir")}/reports/report.txt").toFile()
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }


}
