package net.asere.omni.mvi

fun <State, Effect, Action> Container<State, Effect, Action>
        .asStateContainer() = this as StateContainer