package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import net.asere.omni.core.ContainerHost
import net.asere.omni.core.OmniHostDsl
import net.asere.omni.core.execute

/**
 * Implement this interface to turn your class
 * into a state host.
 */
interface StateContainerHost<State, Effect> : ContainerHost {
    override val container: StateContainer<State, Effect>
}

/**
 * Current and last state emitted from within the state host
 */
val <State> StateContainerHost<State, *>.currentState: State
    get() = container.asStateContainer().state.value

/**
 * Executes an intent
 *
 * @param context Defines the context to run instructions
 * @param start CoroutineStart policy
 * @param block Executable intent content
 */
@OmniHostDsl
fun <State, Effect> StateContainerHost<State, Effect>.intent(
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
 * Executes an intent and returns its job
 *
 * @param context Defines the context to run instructions
 * @param start CoroutineStart policy
 * @param block Executable intent content
 *
 * @return the job running the intent
 */
@OmniHostDsl
fun <State, Effect> StateContainerHost<State, Effect>.intentJob(
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
 * Emits each state to the provided callback
 *
 * @param onState Block set here will receive emitted states
 */
fun <State> StateContainerHost<State, *>.observeState(onState: (State) -> Unit) = intent {
    container.state.collect { onState(it) }
}

/**
 * Emits each effect to the provided callback
 *
 * @param onEffect Block set here will receive emitted effects
 */
fun <Effect> StateContainerHost<*, Effect>.observeEffect(onEffect: (Effect) -> Unit) = intent {
    container.effect.collect { onEffect(it) }
}