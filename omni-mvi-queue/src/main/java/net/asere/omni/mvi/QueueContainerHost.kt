package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * A specialized [StateContainerHost] that supports sequential intent execution via a queue.
 *
 * Implement this interface in your ViewModel or host class to enable the `queueIntent`
 * and `clearQueue` DSL functions.
 */
interface QueueContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    /**
     * The [StateContainer] managed by this host.
     */
    override val container: StateContainer<State, Effect>
}

/**
 * Enqueues an intent for sequential execution.
 *
 * Intents enqueued this way are guaranteed to execute one after another in the order
 * they were called.
 *
 * @param block The suspendable logic to be executed.
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        QueueContainerHost<State, Effect>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)

/**
 * Clears the current intent queue of the hosted container.
 *
 * This stops all pending and active queued intents.
 */
fun <State : Any, Effect : Any>
        QueueContainerHost<State, Effect>.clearQueue() =
    container.asQueueContainer().clearQueue()
