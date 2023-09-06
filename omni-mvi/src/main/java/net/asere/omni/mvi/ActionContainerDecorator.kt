package net.asere.omni.mvi

/**
 * This is the actual Action Container implementation. It decorates any container
 * and turns it into an Action container
 */
class ActionContainerDecorator<State, Effect, Action>(
    override val onAction: (Action) -> Unit,
    container: Container<State, Effect>,
) : ContainerDecorator<State, Effect>(
    container = container
), ActionContainer<State, Effect, Action>