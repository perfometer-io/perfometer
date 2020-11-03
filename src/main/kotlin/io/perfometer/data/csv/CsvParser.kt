package io.perfometer.data.csv

internal class CsvParser(private val csvText: String, private val delimiter: Char = ',') {

    private var position = 0

    fun parse(): List<List<String>> {
        position = 0
        val result = mutableListOf<List<String>>()
        while (isNotFinished()) {
            result.add(parseRecord())
        }
        return result
    }

    private fun parseRecord(): List<String> {
        val result = mutableListOf<String>()
        while (isNotCurrentChar('\n')) {
            result.add(parseField())
        }
        while (isCurrentChar('\n', '\r')) position++
        return result
    }

    private fun parseField(): String {
        return if (csvText[position] == '"') parseQuotedField() else parseUnquotedField()
    }

    private fun parseQuotedField(): String {
        position++
        val start = position
        while (isNotCurrentChar('"') || isNextChar('"')) {
            if (isCurrentChar('"') && isNextChar('"')) position++
            position++
        }
        val end = position
        position++
        while (isNotCurrentChar(delimiter, '\n')) position++
        if (isCurrentChar(delimiter)) position++
        return csvText.substring(start, end).replace("\"\"", "\"")
    }

    private fun parseUnquotedField(): String {
        val start = position
        while (isNotCurrentChar(delimiter, '\n')) position++
        val end = position
        if (isCurrentChar(delimiter)) position++
        return csvText.substring(start, end)
    }

    private fun isCurrentChar(vararg chars: Char): Boolean =
        isNotFinished() && chars.contains(csvText[position])

    private fun isNotCurrentChar(vararg chars: Char): Boolean =
        isNotFinished() && !chars.contains(csvText[position])

    private fun isNextChar(vararg chars: Char): Boolean =
        position + 1 < csvText.length && chars.contains(csvText[position+1])

    private fun isNotFinished() = position < csvText.length
}
