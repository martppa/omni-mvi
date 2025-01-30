package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * Turn an object into a QueueContainer
 */
interface QueueContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    override val container: StateContainer<State, Effect>
}

/**
 * Enqueue an intent
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        QueueContainerHost<State, Effect>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)

/**
 * Clear container's intent queue
 */
fun <State : Any, Effect : Any>
        QueueContainerHost<State, Effect>.clearQueue() =
    container.asQueueContainer().clearQueue()