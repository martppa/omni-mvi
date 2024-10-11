package net.asere.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Use this container to execute intents stopping their previous execution.
 */
open class OverrideContainer<State, Effect> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect>,
    OverrideContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, Job>()

    /**
     * Start an intent overriding its previous execution
     *
     * @param intentId Intent identifier
     * @param block Intent's content
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

private fun <State, Effect> overrideContainer(
    container: StateContainer<State, Effect>
) = OverrideContainer(container)

/**
 * Turns this container into an override container
 */
fun <State, Effect> StateContainer<State, Effect>
        .buildOverrideContainer() = overrideContainer(this)

/**
 * Seeks for an OverrideContainer from inside a decorated container
 */
internal fun <State, Effect>
        StateContainer<State, Effect>.asOverrideContainer() =
    asStateContainer().seek<OverrideContainer<State, Effect>> {
        it is OverrideContainer<*, *>
    }