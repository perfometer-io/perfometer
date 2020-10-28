package io.perfometer.statistics.consumer

import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class FileTypeExtension(val fileExtension: String) {
    TXT(".txt"), PDF(".pdf"), HTML(".html")
}

abstract class StatisticsFileWriter {

    protected fun buildFilePath(
        startTime: ZonedDateTime,
        extension: FileTypeExtension,
    ): Path {
        return Paths.get(
            "${System.getProperty("user.dir")}/reports/report-${dateTimeFormatter.format(startTime)}${extension.fileExtension}"
        )
    }

    companion object {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
