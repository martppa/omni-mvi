package net.asere.omni.mvi

/**
 * Delegates any state or effect emitted to the inner container
 */
open class DelegatorContainer<State, Effect>(
    container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), InnerStateContainer<State, Effect> {

    private var delegatedContainer: InnerStateContainer<State, Effect>? = null

    /**
     * Sets the container to delegate to
     */
    fun delegate(container: InnerStateContainer<State, Effect>) {
        delegatedContainer = container
    }

    /**
     * Clears delegating container
     */
    fun clearDelegate() {
        delegatedContainer = null
    }

    /**
     * Updates the state and delegates the update
     */
    override fun update(function: State.() -> State) {
        delegatedContainer?.update(function)
        container.asStateContainer().update(function)
    }

    /**
     * Post the effect and delegates it
     */
    override fun post(effect: Effect) {
        delegatedContainer?.post(effect)
        container.asStateContainer().post(effect)
    }
}