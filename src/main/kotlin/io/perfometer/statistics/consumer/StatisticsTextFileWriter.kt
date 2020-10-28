package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.Output.TEXT_FILE
import java.io.File

internal object StatisticsTextFileWriter : StatisticsFileWriter() {

    fun write(scenarioSummary: ScenarioSummary) {
        val formatted: String = StatisticsTextFormatter.format(scenarioSummary)
        val file: File = createReportFile(scenarioSummary.startTime.toZonedDateTimeUTC(), TEXT_FILE)
        file.writeText(formatted)
    }

}
