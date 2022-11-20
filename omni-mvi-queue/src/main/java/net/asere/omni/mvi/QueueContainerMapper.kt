package net.asere.omni.mvi

internal fun <State, Effect, Action>
        Container<State, Effect, Action>.asQueueContainer() =
    seek<QueueContainer<State, Effect, Action>> { it is QueueContainer<*, *, *> }