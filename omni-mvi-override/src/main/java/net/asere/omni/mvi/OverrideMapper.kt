package net.asere.omni.mvi

internal fun <State, Effect, Action>
        Container<State, Effect, Action>.asOverrideContainer() =
    seek<OverrideContainer<State, Effect, Action>> {
        it is OverrideContainer<*, *, *>
    }