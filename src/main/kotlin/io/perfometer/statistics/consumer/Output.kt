package io.perfometer.statistics.consumer

enum class Output(val fileExtension: String? = null) {
    STDOUT, TEXT_FILE(".txt"), HTML(".html"), PDF(".pdf")
}
