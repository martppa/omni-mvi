package net.asere.omni.mvi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

abstract class ExecutableContainer(
    private val coroutineScope: CoroutineScope,
    private val coroutineExceptionHandler: CoroutineExceptionHandler
) {
    private val containerJob = coroutineScope.coroutineContext.job

    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        private const val BlockedExecutionThreadName = "BlockedExecutionThread"
        fun blockedContext() = newSingleThreadContext(BlockedExecutionThreadName)
    }

    private var locked: Boolean = false

    fun releaseExecution() {
        locked = false
    }

    fun lockExecution() {
        locked = true
    }

    private fun isExecutionLocked(): Boolean {
        return Thread.currentThread().name == BlockedExecutionThreadName || locked
    }

    suspend fun awaitJobs() = containerJob.joinChildren()

    fun launchJobs() = containerJob.startChildrenJobs()

    fun execute(
        context: CoroutineContext,
        start: CoroutineStart,
        onError: (throwable: Throwable) -> Unit,
        block: suspend () -> Unit,
    ): Job {
        val startCriteria = if (isExecutionLocked()) {
            CoroutineStart.LAZY
        } else {
            start
        }
        return coroutineScope.launch(
            context = context + coroutineExceptionHandler,
            start = startCriteria
        ) {
            runCatching {
                block()
            }.onCoroutineFailure {
                coroutineExceptionHandler.handleException(context, it)
                onError(it)
            }
        }
    }
}

private fun <T> Result<T>.onCoroutineFailure(block: (Throwable) -> Unit): Result<T> {
    onFailure {
        if (it is CancellationException) throw it
        block(it)
    }
    return this
}