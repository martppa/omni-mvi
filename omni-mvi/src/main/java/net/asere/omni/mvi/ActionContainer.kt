package net.asere.omni.mvi

/**
 * This container exposes an interface to communicate using Actions. Implement this
 * container to turn your container into an action container.
 */
interface ActionContainer<State, Effect, Action> : Container<State, Effect> {
    val onAction: (Action) -> Unit
}

fun <State, Effect, Action> Container<State, Effect>.onAction(
    onAction: (Action) -> Unit
): ActionContainer<State, Effect, Action> = ActionContainerDecorator(onAction, this)