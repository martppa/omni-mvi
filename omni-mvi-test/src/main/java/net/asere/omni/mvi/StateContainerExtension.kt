package net.asere.omni.mvi

val <State : Any, Effect : Any> StateContainer<State, Effect>.stateFlow get() = asStateContainer().state

val <State : Any, Effect : Any> StateContainerHost<State, Effect>.stateFlow get() = container.asStateContainer().state
