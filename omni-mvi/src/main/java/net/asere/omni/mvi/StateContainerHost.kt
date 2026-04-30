package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import net.asere.omni.core.ContainerHost
import net.asere.omni.core.OmniHostDsl
import net.asere.omni.core.execute
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Interface to be implemented by classes that host an MVI state container.
 *
 * This is usually implemented by a ViewModel. It provides convenient extension
 * functions for launching intents, observing state, and observing effects.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 */
interface StateContainerHost<State : Any, Effect : Any> : ContainerHost {
    /**
     * The [StateContainer] managed by this host.
     */
    override val container: StateContainer<State, Effect>
}

/**
 * Returns the current [State] value from the hosted container.
 */
val <State : Any> StateContainerHost<State, *>.currentState: State
    get() = container.asStateContainer().state.value

/**
 * Launches an asynchronous intent.
 *
 * This is the primary way to perform actions that might change the state or post effects.
 *
 * @param context Additional [CoroutineContext] for the intent execution.
 * @param start Coroutine start policy.
 * @param block The block of code to execute within an [IntentScope].
 */
@OmniHostDsl
fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<State, Effect>.() -> Unit
) {
    intentJob(
        context = context,
        start = start,
        block = block
    )
}

/**
 * Executes a block of code directly within an [IntentScope] without launching a new coroutine.
 *
 * This is useful for calling intent-specific logic from a context that already has a scope.
 *
 * @param block The block of code to execute.
 */
@OmniHostDsl
suspend fun <State : Any, Effect : Any, R> StateContainerHost<State, Effect>.intentScope(
    block: suspend IntentScope<State, Effect>.() -> R
): R {
    val scope = IntentScope(container.asStateContainer())
    return block(scope)
}

/**
 * Launches an asynchronous intent and returns the resulting [Job].
 *
 * @param context Additional [CoroutineContext] for the intent execution.
 * @param start Coroutine start policy.
 * @param block The block of code to execute.
 * @return The [Job] representing the intent execution.
 */
@OmniHostDsl
fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.intentJob(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<State, Effect>.() -> Unit
): Job {
    val scope = IntentScope(container.asStateContainer())
    return execute(
        context = context,
        start = start,
        scope = scope,
        block = block
    )
}

/**
 * Executes a block in a suspending [IntentScope] and returns its result.
 *
 * @param block The logic to execute.
 * @return The result of the [block].
 */
@OmniHostDsl
suspend fun <State : Any, Effect : Any, Result> StateContainerHost<State, Effect>.suspendIntent(
    block: suspend IntentScope<State, Effect>.() -> Result
): Result {
    val scope: IntentScope<State, Effect> = IntentScope(container = container.asStateContainer())
    return block(scope)
}

/**
 * Helper function to observe state changes using a simple callback.
 *
 * @param onState The callback that will be triggered for every new state emission.
 * @return The [Job] that is collecting the state flow.
 */
fun <State : Any> StateContainerHost<State, *>.observeState(onState: (State) -> Unit) = intent {
    container.asStateContainer().state.collect { onState(it) }
}

/**
 * Helper function to observe effects using a simple callback.
 *
 * @param onEffect The callback that will be triggered for every new effect emission.
 * @return The [Job] that is collecting the effect flow.
 */
fun <Effect : Any> StateContainerHost<*, Effect>.observeEffect(onEffect: (Effect) -> Unit) = intent {
    container.asStateContainer().effect.collect { onEffect(it) }
}
