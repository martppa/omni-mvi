package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect>
        QueueContainerHost<State, Effect>.queueIntent(
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asQueueContainer().enqueue(block)