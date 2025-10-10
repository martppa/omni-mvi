package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import net.asere.omni.core.ExecutableContainer
import kotlin.coroutines.suspendCoroutine

/**
 * Returns itself as an executable container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asExecutableContainer(): ExecutableContainer =
    asStateContainer().seek { it is ExecutableContainer }

/**
 * Seeks all children jobs and await for their completion. Use this method to await all
 * children executions asynchronously. This method does not join the container job itself.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.await() =
    container.asExecutableContainer().await()

/**
 * Seeks all children jobs and joins them. Use this method to await children jobs sequentially.
 * This method does not join the container job itself.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.joinChildren() =
    container.asExecutableContainer().joinChildren()

/**
 * Seeks all children jobs and cancel them. Use this method to cancel all children executions.
 * This method does not cancel the container job itself.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.cancelOngoingExecutions() =
    container.asExecutableContainer().stop()

/**
 * Awaits all running jobs until the provided condition is met.
 *
 * @param until Condition to meet in order to continue waiting for jobs.
 * @param mode Mode of executions.
 * - Sequential: Jobs will be completed the same order they were created
 * - Async: Jobs will run asynchronously until completion
 */
suspend fun <State : Any, Effect : Any>
        StateContainer<State, Effect>.await(mode: RunMode, until: () -> Boolean) {
    val awaitingJob = coroutineScope.launch(start = CoroutineStart.LAZY) {
        when (mode) {
            RunMode.Async -> await()
            RunMode.Sequential -> joinChildren()
        }
    }
    suspendCoroutine { continuation ->
        fun verify() {
            if (until()) {
                clearDelegate()
                awaitingJob.cancelChildren()
                awaitingJob.cancel()
            }
        }
        delegate(
            doOnAnyEmission(
                container = this@await.asStateContainer(),
                block = ::verify
            )
        )
        coroutineScope.launch {
            fun resume() {
                continuation.resumeWith(Result.success(Unit))
            }
            this@await.onError {
                resume()
            }
            awaitingJob.join()
            resume()
        }
    }
}

/**
 * Performs an action everytime a state or an effect is emitted
 *
 * @param container Container to delegate
 * @param block Code to execute
 *
 * @return Delegated container
 */
private fun <State : Any, Effect : Any> doOnAnyEmission(
    container: InnerStateContainer<State, Effect>,
    block: () -> Unit
): InnerStateContainer<State, Effect> {
    return object : DelegatorContainer<State, Effect>(container) {
        override fun update(function: State.() -> State) {
            super.update(function)
            block()
        }

        override fun post(effect: Effect) {
            super.post(effect)
            block()
        }
    }
}

/**
 * Start children jobs (not recursively)
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.launchJobs() =
    container.asExecutableContainer().launchJobs()

/**
 * Release the execution of intents in the container. This means unblock executions when
 * running under a blocked context. Any holding execution will be started.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

/**
 * When running under a blocked context this method will force the block of executions.
 * Lock the execution of intents in the container. This means, running executions will
 * put on hold.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.lockExecution() =
    container.asExecutableContainer().lockExecution()

/**
 * Recursively seeks a delegator container and return it
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asDelegatorContainer(): DelegatorContainer<State, Effect> =
    asStateContainer().seek { it is DelegatorContainer<*, *> }

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

/**
 * Clears delegating container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = this.container.delegate(container)

/**
 * Joins container children sequentially
 */
suspend fun <State : Any, Effect : Any> StateContainer<State, Effect>.joinChildren() =
    asExecutableContainer().joinChildren()

/**
 * Await container children jobs completion asynchronously
 */
suspend fun <State : Any, Effect : Any> StateContainer<State, Effect>.await() =
    asExecutableContainer().await()
