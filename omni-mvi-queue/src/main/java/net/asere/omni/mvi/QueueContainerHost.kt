package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

interface QueueContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

@OmniHostDsl
fun <State, Effect>
        QueueContainerHost<State, Effect>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)

fun <State, Effect>
        QueueContainerHost<State, Effect>.clearQueue() =
    container.asQueueContainer().clearQueue()