package io.perfometer.data

import io.perfometer.data.csv.CsvParser
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType

interface DataSource<T> {
    val size: Int
    operator fun get(index: Int): T
}

class ListDataSource<T>(private val list: List<T>) : DataSource<T> {
    override val size: Int = list.size
    override fun get(index: Int): T = list[index]
}

class CsvDataSource<T : Any>(
    private val clazz: KClass<T>,
    csvFilePath: String,
    private val hasHeader: Boolean = false,
    private val delimiter: Char = ',',
) : DataSource<T> {

    private val csvFile = File(csvFilePath)
    private val data: List<T> = readData();

    private fun readData(): List<T> {
        val csvRecords = CsvParser(csvFile.readText(), delimiter).parse()
        val constructor = clazz.constructors.first()
        val records = if (hasHeader) csvRecords.subList(1, csvRecords.size) else csvRecords
        return records.map {
            val values = it.mapIndexed { index, value ->
                val parameter = constructor.parameters[index]
                convertToType(parameter, value)
            }.toTypedArray()
            constructor.call(*values)
        }
    }

    private fun convertToType(parameter: KParameter, value: String): Any {
        return when (parameter.type) {
            Int::class.createType() -> value.toInt()
            Long::class.createType() -> value.toLong()
            Byte::class.createType() -> value.toByte()
            Short::class.createType() -> value.toShort()
            BigDecimal::class.createType() -> value.toBigDecimal()
            BigInteger::class.createType() -> value.toBigInteger()
            Double::class.createType() -> value.toDouble()
            Float::class.createType() -> value.toFloat()
            Regex::class.createType() -> value.toRegex()
            else -> value
        }
    }

    override val size: Int = data.size
    override fun get(index: Int): T = data[index]
}
