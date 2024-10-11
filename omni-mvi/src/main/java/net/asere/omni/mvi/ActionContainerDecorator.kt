package net.asere.omni.mvi

/**
 * This is the actual Action Container implementation. It decorates any container
 * and turns it into an Action container
 */
class ActionContainerDecorator<State, Effect, Action>(
    override val onAction: (Action) -> Unit,
    container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container = container
), InnerActionContainer<State, Effect, Action>