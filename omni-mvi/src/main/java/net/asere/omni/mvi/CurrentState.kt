package net.asere.omni.mvi

val <State> StateContainerHost<State, *>.currentState: State
    get() = container.asStateContainer().state.value