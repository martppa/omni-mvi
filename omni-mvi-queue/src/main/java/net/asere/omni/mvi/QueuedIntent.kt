package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect, Action>
        QueueContainerHost<State, Effect, Action>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)