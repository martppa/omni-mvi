package net.asere.omni.mvi

fun <State, Effect, Action> queueContainer(
    container: Container<State, Effect, Action>
) = QueueContainer(container)