package net.asere.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class OverrideContainer<State, Effect> internal constructor(
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), ExposedStateContainer<State, Effect>,
    OverrideContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, Job>()

    internal fun overrideIntent(
        intentId: Any = Unit,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        mutex.withLock {
            val job = intents[intentId]
            job?.cancel()
            job?.join()
            intents[intentId] = intent { block() }
        }
    }
}

fun <State, Effect> overrideContainer(
    container: ExposedStateContainer<State, Effect>
) = OverrideContainer(container)

fun <State, Effect> ExposedStateContainer<State, Effect>
        .buildOverrideContainer() = overrideContainer(this)

internal fun <State, Effect>
        ExposedStateContainer<State, Effect>.asOverrideContainer() =
    asStateContainer().seek<OverrideContainer<State, Effect>> {
        it is OverrideContainer<*, *>
    }