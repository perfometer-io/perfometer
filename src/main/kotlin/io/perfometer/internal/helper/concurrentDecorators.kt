package io.perfometer.internal.helper

internal suspend fun decorateSuspendingInterruptable(action: suspend () -> Unit) {
    try {
        action.invoke()
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}

internal fun decorateInterruptable(action: () -> Unit) {
    try {
        action.invoke()
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}
