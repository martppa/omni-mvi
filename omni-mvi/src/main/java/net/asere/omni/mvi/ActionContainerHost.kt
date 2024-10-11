package net.asere.omni.mvi

/**
 * Action container's Host
 */
interface ActionContainerHost<State, Effect, Action> : StateContainerHost<State, Effect> {
    override val container: ActionContainer<State, Effect, Action>
}

/**
 * Call this function to pass actions to the container. Calling this function will
 * trigger onAction block
 *
 * @param action Defined action type in the container and host
 *
 * @see InnerActionContainer.onAction
 */
fun <State, Effect, Action>
        ActionContainerHost<State, Effect, Action>.on(action: Action) {
    container.asActionContainer().onAction(action)
}

internal fun <State, Effect, Action> ActionContainer<State, Effect, Action>
        .asActionContainer() = this as InnerActionContainer<State, Effect, Action>