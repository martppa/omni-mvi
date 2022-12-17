package net.asere.omni.mvi

internal fun <State, Effect>
        Container<State, Effect>.asQueueContainer() =
    seek<QueueContainer<State, Effect>> { it is QueueContainer<*, *> }