package net.asere.omni.mvi

/**
 * A [StateContainerDecorator] that delegates state updates and effects to another container.
 *
 * This allows for intercepting or duplicating state and effect emissions. By setting a
 * [delegatedContainer], any call to [update] or [post] will be forwarded to both the
 * decorated container and the delegate.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @param container The original container to be decorated.
 */
open class DelegatorContainer<State : Any, Effect : Any>(
    container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
),
    InnerStateContainer<State, Effect> {

    private var delegatedContainer: InnerStateContainer<State, Effect>? = null

    /**
     * Sets the container that will also receive state updates and effects.
     */
    fun delegate(container: InnerStateContainer<State, Effect>) {
        delegatedContainer = container
    }

    /**
     * Removes the current delegate container.
     */
    fun clearDelegate() {
        delegatedContainer = null
    }

    /**
     * Updates the state in both the decorated container and the delegate (if present).
     */
    override fun update(function: State.() -> State) {
        delegatedContainer?.update(function)
        container.asStateContainer().update(function)
    }

    /**
     * Posts an effect to both the decorated container and the delegate (if present).
     */
    override fun post(effect: Effect) {
        delegatedContainer?.post(effect)
        container.asStateContainer().post(effect)
    }
}
