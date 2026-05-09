package net.asere.omni.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Use this object as a default handler for coroutine
 * exceptions if you don't want to apply any specific error handling logic.
 */
val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

/**
 * An abstract [Container] capable of executing intents within a provided [CoroutineScope]
 * and [CoroutineExceptionHandler]. Most specialized containers in Omni MVI extend this class.
 *
 * It provides mechanisms for:
 * - Executing blocks of code asynchronously ([execute]).
 * - Managing execution lifecycle (wait, cancel, join).
 * - Locking/unlocking execution (useful for testing or specific synchronization needs).
 *
 * @param coroutineScope The scope used for launching jobs.
 * @param coroutineExceptionHandler The handler for uncaught exceptions.
 */
abstract class ExecutableContainer(
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler
) : Container {
    private val containerJob by lazy { coroutineScope.coroutineContext.job }

    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        private const val BLOCKED_EXECUTION_THREAD_NAME = "BlockedExecutionThread"

        /**
         * Creates a new [CoroutineContext] that uses a single dedicated thread
         * intended for blocked execution scenarios.
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun blockedContext() = newSingleThreadContext(BLOCKED_EXECUTION_THREAD_NAME)
    }

    private var locked: Boolean = false

    /**
     * Resumes the execution of intents in the container.
     *
     * If the container was previously locked or running under a blocked context,
     * this will allow queued or future executions to proceed.
     */
    fun releaseExecution() {
        locked = false
    }

    /**
     * Prevents new intent executions from starting immediately.
     *
     * When locked, any call to [execute] will result in a [CoroutineStart.LAZY] job
     * that won't start until [releaseExecution] is called or the job is explicitly started.
     */
    fun lockExecution() {
        locked = true
    }

    /**
     * Checks if intent execution is currently restricted.
     *
     * Execution is considered locked if [lockExecution] was called or if the
     * current thread is the dedicated blocked execution thread.
     */
    private fun isExecutionLocked(): Boolean {
        return Thread.currentThread().name == BLOCKED_EXECUTION_THREAD_NAME || locked
    }

    /**
     * Suspends until all active child jobs within this container's scope have completed.
     *
     * This method does not join or cancel the container's main job itself.
     */
    suspend fun await() {
        coroutineScope {
            containerJob.children.map { async { it.join() } }.toList().awaitAll()
        }
    }

    /**
     * Joins all child jobs sequentially.
     *
     * Unlike [await], this iterates through children and joins them one by one.
     */
    suspend fun joinChildren() = containerJob.joinChildren()

    /**
     * Cancels all child jobs currently running in this container.
     *
     * The container's scope remains active, allowing for future executions.
     */
    fun cancel() = containerJob.cancelChildren()

    /**
     * Joins the main job associated with the container's [CoroutineScope].
     */
    suspend fun join() = containerJob.join()

    /**
     * Explicitly starts all child jobs that are currently in a lazy state.
     */
    fun launchJobs() = containerJob.startChildren()

    /**
     * Launches a new coroutine to execute a block of code.
     *
     * @param context Additional [CoroutineContext] to be merged with the container's context.
     * @param start The [CoroutineStart] policy. If execution is locked, this is overridden to [CoroutineStart.LAZY].
     * @param onError Callback triggered if an exception occurs during the execution of [block].
     * @param block The suspendable block of code to execute.
     * @return The [Job] representing the execution.
     */
    fun execute(
        context: CoroutineContext,
        start: CoroutineStart,
        onError: suspend (throwable: Throwable) -> Unit,
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

/**
 * Extension to handle failures in a coroutine-friendly way.
 * Re-throws [CancellationException] to ensure proper coroutine cancellation flow.
 */
private inline fun <T> Result<T>.onCoroutineFailure(block: (Throwable) -> Unit): Result<T> {
    onFailure {
        if (it is CancellationException) throw it
        block(it)
    }
    return this
}

/**
 * Executes a block of code within the [ExecutableContainer] hosted by this [ContainerHost].
 *
 * This is the primary DSL entry point for running intents in a [ContainerHost].
 *
 * @param context The [CoroutineContext] for the execution.
 * @param start The [CoroutineStart] policy.
 * @param scope An [ExecutionScope] that provides local error handling.
 * @param block The block of code to execute within the [scope].
 * @return The [Job] representing the asynchronous execution.
 * @throws IllegalStateException If the hosted [Container] is not an [ExecutableContainer].
 */
@OmniHostDsl
fun <Scope : ExecutionScope> ContainerHost.execute(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    scope: Scope,
    block: suspend Scope.() -> Unit
): Job {
    suspend fun onError(throwable: Throwable) {
        scope.errorBlock(throwable)
    }
    if (container !is ExecutableContainer)
        throw IllegalStateException("The container is not an Executable container!")
    return (container as ExecutableContainer).execute(
        context = context,
        start = start,
        onError = ::onError,
    ) {
        scope.block()
    }
}
