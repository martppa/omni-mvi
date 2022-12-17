package net.asere.omni.mvi

interface ActionContainer<State, Effect, Action> : Container<State, Effect> {
    val onAction: (Action) -> Unit
}

fun <State, Effect, Action> Container<State, Effect>.onAction(onAction: (Action) -> Unit) =
    ActionContainerDecorator(onAction, this)