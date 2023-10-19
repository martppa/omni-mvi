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
import kotlin.coroutines.EmptyCoroutineContext

val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

abstract class ExecutableContainer(
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler
) : Container {
    private val containerJob by lazy { coroutineScope.coroutineContext.job }

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

fun Container.asExecutableContainer(): ExecutableContainer {
    if (this is StateContainerDecorator<*, *>) return seek { it is ExecutableContainer }
    if (this is ExecutableContainer) return this as ExecutableContainer
    throw IllegalStateException("The container is not an Executable container!")
}

@StateHostDsl
fun ContainerHost.execute(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend ExecutionScope.() -> Unit
) = execute(
    context = context,
    start = start,
    scope = ExecutionScope(container),
    block = block
)

@StateHostDsl
fun ContainerHost.execute(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    scope: ExecutionScope,
    block: suspend ExecutionScope.() -> Unit
): Job {
    fun onError(throwable: Throwable) {
        scope.errorBlock(throwable)
    }
    val executableContainer = container.asExecutableContainer()
    return executableContainer.execute(
        context = context,
        start = start,
        onError = ::onError,
    ) {
        scope.block()
    }
}