package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.asere.omni.core.ExecutableContainer
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine

/**
 * Returns itself as an executable container
 */
fun <State, Effect> ExposedStateContainer<State, Effect>.asExecutableContainer(): ExecutableContainer =
    asStateContainer().seek { it is ExecutableContainer }

/**
 * Seeks all children jobs and await them. Use this method to await all children executions.
 * This method does not join the container job itself.
 */
suspend fun <State, Effect>
        StateContainerHost<State, Effect>.await() =
    container.asExecutableContainer().await()

/**
 * Recursively seeks all nested children jobs and await them.
 * Use this method to await all nested children executions. This method does not
 * join the container job itself.
 */
suspend fun <State, Effect>
        StateContainerHost<State, Effect>.deepAwait() =
    container.asExecutableContainer().deepAwait()

/**
 * Recursively awaits all running jobs until the provided condition is met.
 *
 * @param until Condition to meet in order to stop waiting for jobs.
 */
suspend fun <State, Effect> ExposedStateContainer<State, Effect>.deepAwait(
    scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    until: () -> Boolean,
) {
    val awaitingJob = scope.launch(start = CoroutineStart.LAZY) {
        asExecutableContainer().deepAwait()
    }
    suspendCoroutine { continuation ->
        scope.launch {
            while (!until()) {
                delay(100)
            }
            awaitingJob.cancelChildren()
            awaitingJob.cancel()
        }
        scope.launch {
            awaitingJob.join()
            continuation.resumeWith(Result.success(Unit))
        }
    }
}

/**
 * Start children jobs (not recursively)
 */
fun <State, Effect>
        StateContainerHost<State, Effect>.launchJobs() =
    container.asExecutableContainer().launchJobs()

/**
 * Release the execution of intents in the container
 */
fun <State, Effect>
        StateContainerHost<State, Effect>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

/**
 * Lock the execution of intents in the container
 */
fun <State, Effect>
        StateContainerHost<State, Effect>.lockExecution() =
    container.asExecutableContainer().lockExecution()

/**
 * Recursively seeks a delegator container and return it
 */
fun <State, Effect> ExposedStateContainer<State, Effect>.asDelegatorContainer(): DelegatorContainer<State, Effect> =
    asStateContainer().seek { it is DelegatorContainer<*, *> }

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State, Effect> ExposedStateContainer<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

/**
 * Clears delegating container
 */
fun <State, Effect> ExposedStateContainer<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State, Effect> StateContainerHost<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = this.container.delegate(container)

/**
 * Clears delegating container
 */
fun <State, Effect> StateContainerHost<State, Effect>.clearDelegate() =
    container.clearDelegate()