package net.asere.omni.mvi

fun <State, Effect> queueContainer(
    container: Container<State, Effect>
) = QueueContainer(container)