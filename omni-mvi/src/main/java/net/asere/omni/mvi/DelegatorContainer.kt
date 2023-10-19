package net.asere.omni.mvi

/**
 * Delegates any state or effect emitted to the inner container
 */
open class DelegatorContainer<State, Effect>(
    container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect> {

    private var delegatedContainer: StateContainer<State, Effect>? = null

    /**
     * Sets the container to delegate to
     */
    fun delegate(container: StateContainer<State, Effect>) {
        delegatedContainer = container
    }

    /**
     * Clears delegating container
     */
    fun clearDelegate() {
        delegatedContainer = null
    }

    override fun update(function: State.() -> State) {
        delegatedContainer?.update(function)
        container.asStateContainer().update(function)
    }

    override fun post(effect: Effect) {
        delegatedContainer?.post(effect)
        container.asStateContainer().post(effect)
    }
}