package net.asere.omni.mvi

import kotlinx.coroutines.launch
import net.asere.omni.core.ExecutionScope
import net.asere.omni.core.OmniHostDsl

/**
 * A specialized [ExecutionScope] for executing MVI intents.
 *
 * It provides access to the [InnerStateContainer], allowing for state updates (reduce)
 * and posting side effects.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The container that this scope operates on.
 */
class IntentScope<State : Any, Effect : Any>(
    val container: InnerStateContainer<State, Effect>,
) : ExecutionScope()

/**
 * Updates the current state of the container.
 */
@Deprecated("Use reduce extension function", ReplaceWith("reduce(function)"))
@OmniHostDsl
fun <State : Any> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

/**
 * Transforms the current state into a new state.
 *
 * This is the primary way to perform state transitions in Omni MVI.
 *
 * @param function A lambda that receives the current state and returns the updated state.
 */
@OmniHostDsl
fun <State : Any> IntentScope<State, *>.reduce(
    function: State.() -> State
) = container.update(function)

/**
 * Posts a side effect to the container.
 */
@Deprecated("Use post extension function", ReplaceWith("post(effect)"))
@OmniHostDsl
fun <Effect : Any> IntentScope<*, Effect>.postEffect(
    effect: Effect
) = container.post(effect)

/**
 * Emits one or more side effects.
 *
 * Side effects are one-off events like navigation, showing a toast, or playing a sound.
 *
 * @param effects One or more effect instances to be emitted.
 */
@OmniHostDsl
fun <Effect : Any> IntentScope<*, Effect>.post(
    vararg effects: Effect
) {
    effects.forEach { container.post(it) }
}

/**
 * Executes a suspending block and waits for its completion.
 *
 * This behaves similarly to `runBlocking`, but it is coroutine-friendly and
 * executes within the container's [kotlinx.coroutines.CoroutineScope].
 *
 * @param block The suspendable code to run and await.
 */
@OmniHostDsl
suspend fun IntentScope<*, *>.join(
    block: suspend () -> Unit
) {
    container.coroutineScope.launch {
        block()
    }.join()
}
