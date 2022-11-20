package net.asere.omni.mvi

fun <State, Effect, Action>
        QueueContainerHost<State, Effect, Action>.clearQueue() =
    container.asQueueContainer().clearQueue()