package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * Turn an object into a QueueContainer
 */
interface QueueContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

/**
 * Enqueue an intent
 */
@OmniHostDsl
fun <State, Effect>
        QueueContainerHost<State, Effect>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)

/**
 * Clear intent's queue
 */
fun <State, Effect>
        QueueContainerHost<State, Effect>.clearQueue() =
    container.asQueueContainer().clearQueue()