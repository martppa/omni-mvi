package net.asere.omni.mvi

class ActionContainerDecorator<State, Effect, Action>(
    override val onAction: (Action) -> Unit,
    container: Container<State, Effect>,
) : ContainerDecorator<State, Effect>(
    container = container
), ActionContainer<State, Effect, Action>