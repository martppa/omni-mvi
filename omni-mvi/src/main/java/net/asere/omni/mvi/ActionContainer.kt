package net.asere.omni.mvi

/**
 * This container exposes an interface to communicate using Actions. Implement this
 * container to turn your container into an action container.
 */
interface ActionContainer<State, Effect, Action> :
    StateContainer<State, Effect>, ExposedActionContainer<State, Effect, Action> {
    val onAction: (Action) -> Unit
}

/**
 * Sets the action listener and returns itself (the container) as an action container
 */
fun <State, Effect, Action> StateContainer<State, Effect>.onAction(
    onAction: (Action) -> Unit
): ActionContainer<State, Effect, Action> =
    ActionContainerDecorator(onAction, this.asStateContainer())