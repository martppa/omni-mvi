package net.asere.omni.mvi

fun <State, Effect>
        QueueContainerHost<State, Effect>.clearQueue() =
    container.asQueueContainer().clearQueue()