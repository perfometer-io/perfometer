package io.perfometer.statistics.consumer

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

abstract class StatisticsFileWriter {

    protected fun createReportFile(
        startTime: ZonedDateTime,
        output: Output,
    ): File {
        val reportFile = buildFilePath(startTime, output).toFile()
        if (!reportFile.parentFile.exists()) {
            reportFile.parentFile.mkdirs()
        }
        if (!reportFile.exists()) {
            reportFile.createNewFile()
        }
        return reportFile
    }

    private fun buildFilePath(
        startTime: ZonedDateTime,
        output: Output,
    ): Path {
        return Paths.get(
            "${System.getProperty("user.dir")}/reports/report-${dateTimeFormatter.format(startTime)}${output.fileExtension}"
        )
    }

    companion object {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
