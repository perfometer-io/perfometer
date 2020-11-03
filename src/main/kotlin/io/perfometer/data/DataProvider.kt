package io.perfometer.data

import java.util.*
import java.util.concurrent.atomic.AtomicInteger

interface DataProviderStrategy {
    fun nextIndex(size: Int): Int
}

class RandomDataProviderStrategy() : DataProviderStrategy {
    private val random = Random()
    override fun nextIndex(size: Int): Int = random.nextInt(size)
}

class CircularDataProviderStrategy() : DataProviderStrategy {
    private val currentIndex = AtomicInteger(0)
    override fun nextIndex(size: Int): Int = currentIndex.getAndUpdate { (it + 1) % size }
}

class DataProvider<T>(private val from: DataSource<T>, private val strategy: DataProviderStrategy) {
    fun next(): T {
        return from[strategy.nextIndex(from.size)]
    }
}
