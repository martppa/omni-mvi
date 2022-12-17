package net.asere.omni.mvi

interface ActionContainerHost<State, Effect, Action> : StateContainerHost<State, Effect> {
    override val container: ActionContainer<State, Effect, Action>
}

fun <State, Effect, Action>
        ActionContainerHost<State, Effect, Action>.on(action: Action) {
    container.onAction(action)
}