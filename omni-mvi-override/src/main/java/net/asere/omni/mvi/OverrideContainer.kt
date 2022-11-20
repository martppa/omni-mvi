package net.asere.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class OverrideContainer<State, Effect, Action> internal constructor(
    override val container: Container<State, Effect, Action>,
) : ContainerDecorator<State, Effect, Action>(
    container
), Container<State, Effect, Action>,
    OverrideContainerHost<State, Effect, Action> {

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