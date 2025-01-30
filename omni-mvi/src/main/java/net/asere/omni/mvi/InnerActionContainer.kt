package net.asere.omni.mvi

/**
 * This container exposes an interface to communicate using Actions. Implement this
 * container to turn your container into an action container.
 */
interface InnerActionContainer<State : Any, Effect : Any, Action : Any> :
    InnerStateContainer<State, Effect>, ActionContainer<State, Effect, Action> {
    val onAction: (Action) -> Unit
}

/**
 * Sets the action listener and returns itself (the container) as an action container
 */
fun <State : Any, Effect : Any, Action : Any> InnerStateContainer<State, Effect>.onAction(
    onAction: (Action) -> Unit
): InnerActionContainer<State, Effect, Action> =
    ActionContainerDecorator(onAction, this.asStateContainer())