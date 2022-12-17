package net.asere.omni.mvi

internal fun <State, Effect>
        Container<State, Effect>.asOverrideContainer() =
    seek<OverrideContainer<State, Effect>> {
        it is OverrideContainer<*, *>
    }