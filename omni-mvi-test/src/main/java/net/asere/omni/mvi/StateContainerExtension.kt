package net.asere.omni.mvi

/**
 * Convenience extension to access the state flow from a [StateContainer].
 */
val <State : Any, Effect : Any> StateContainer<State, Effect>.stateFlow 
    get() = asStateContainer().state

/**
 * Convenience extension to access the state flow from a [StateContainerHost].
 */
val <State : Any, Effect : Any> StateContainerHost<State, Effect>.stateFlow 
    get() = container.asStateContainer().state
