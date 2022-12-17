package net.asere.omni.mvi

fun <State, Effect> overrideContainer(
    container: Container<State, Effect>
) = OverrideContainer(container)