package net.asere.omni.core

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

/**
 * Use this object as a default handler for coroutine
 * exceptions if you don't want to apply any.
 */
val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

/**
 * This container is capable of executing intents under provided scope
 * and handler. A huge percent of existing containers extend this one.
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
         * Creates a new blocked context
         */
        fun blockedContext() = newSingleThreadContext(BLOCKED_EXECUTION_THREAD_NAME)
    }

    private var locked: Boolean = false


    /**
     * Unblock executions when running under a
     * blocked context
     */
    fun releaseExecution() {
        locked = false
    }

    /**
     * When running under a blocked context this method
     * will force the block of executions
     */
    fun lockExecution() {
        locked = true
    }

    /**
     * Returns whether are the executions blocked or not
     */
    private fun isExecutionLocked(): Boolean {
        return Thread.currentThread().name == BLOCKED_EXECUTION_THREAD_NAME || locked
    }

    /**
     * Seeks all children jobs and await them. Use this method to await all children executions.
     * This method does not join the job itself.
     */
    suspend fun await() = containerJob.joinChildren()

    /**
     * Recursively seeks all children and nested jobs and
     * await them. Use this method to await for all nested underlying executions. This method does
     * not join the job itself.
     */
    suspend fun deepAwait() = containerJob.recursiveJoinChildren()

    /**
     * Join container job
     */
    suspend fun join() = containerJob.join()

    /**
     * Start all children jobs
     */
    fun launchJobs() = containerJob.startChildren()

    /**
     * Executes a block of code under specified context using container's scope.
     *
     * @param context Defines the context to run instructions
     * @param start CoroutineStart policy
     * @param onError Will be triggered everytime an error occurs during execution
     * @param block Executable content
     */
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

/**
 * Executes a block of code under specified context using container's scope.
 *
 * @param context Defines the context to run instructions
 * @param start CoroutineStart policy
 * @param scope Execution scope (not a coroutine scope)
 * @param block Executable content
 */
@OmniHostDsl
fun <Scope : ExecutionScope> ContainerHost.execute(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    scope: Scope,
    block: suspend Scope.() -> Unit
): Job {
    fun onError(throwable: Throwable) {
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
