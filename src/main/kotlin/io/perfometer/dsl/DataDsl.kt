package io.perfometer.dsl

import io.perfometer.data.*
import kotlin.reflect.KClass

class DataProviderConfiguration<T : Any> {
    var from: DataSource<T> = ListDataSource(emptyList())
    var strategy: DataProviderStrategy = CircularDataProviderStrategy()

    fun fromList(list: List<T>) {
        from = ListDataSource(list)
    }

    fun fromCsv(clazz: KClass<T>, csvFilePath: String, hasHeader: Boolean = false, delimiter: Char = ',') {
        from = CsvDataSource(clazz, csvFilePath, hasHeader, delimiter)
    }

    fun circular() {
        strategy = CircularDataProviderStrategy()
    }

    fun random() {
        strategy = RandomDataProviderStrategy()
    }
}

fun <T : Any> data(builder: DataProviderConfiguration<T>.() -> Unit): DataProvider<T> {
    val configuration = DataProviderConfiguration<T>().apply(builder)
    return DataProvider(configuration.from, configuration.strategy)
}
