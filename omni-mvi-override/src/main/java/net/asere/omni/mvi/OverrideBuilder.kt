package net.asere.omni.mvi

fun <State, Effect, Action> overrideContainer(
    container: Container<State, Effect, Action>
) = OverrideContainer(container)