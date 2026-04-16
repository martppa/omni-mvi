package net.asere.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A specialized [StateContainerDecorator] that allows intents to override previous executions.
 *
 * When an intent is launched via this container with an [intentId] that is currently running,
 * the existing execution is canceled and joined before the new one starts. This pattern is
 * commonly known as "restartable" or "latest" intent execution.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner [StateContainer] to be decorated with override capabilities.
 */
open class OverrideContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect>,
    OverrideContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, Job>()

    /**
     * Starts an intent, overriding its previous execution if it exists.
     *
     * This method ensures thread-safety using a [Mutex] and manages the lifecycle
     * of the [Job] associated with the [intentId].
     *
     * @param intentId The identifier for the intent. Defaults to [Unit].
     * @param block The suspendable logic to execute.
     */
    internal fun overrideIntent(
        intentId: Any = Unit,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        mutex.withLock {
            val job = intents[intentId]
            job?.cancel()
            job?.join()
            intents[intentId] = intentJob { block() }
        }
    }
}

/**
 * Internal factory function to create an [OverrideContainer].
 */
private fun <State : Any, Effect : Any> overrideContainer(
    container: StateContainer<State, Effect>
) = OverrideContainer(container)

/**
 * Extension to wrap an existing [StateContainer] into an [OverrideContainer].
 *
 * @return A new [OverrideContainer] instance decorating the original one.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>
        .buildOverrideContainer() = overrideContainer(this)

/**
 * Searches the decoration chain for an [OverrideContainer].
 *
 * @return The [OverrideContainer] found in the stack.
 * @throws RuntimeException if no [OverrideContainer] is found.
 */
internal fun <State : Any, Effect : Any>
        StateContainer<State, Effect>.asOverrideContainer() =
    asStateContainer().seek<OverrideContainer<State, Effect>> {
        it is OverrideContainer<*, *>
    }
