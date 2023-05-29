package net.asere.omni.mvi

open class DelegatorContainer<State, Effect>(
    container: Container<State, Effect>,
) : ContainerDecorator<State, Effect>(
    container
), Container<State, Effect> {

    private var delegatedContainer: StateContainer<State, Effect>? = null

    fun delegate(container: StateContainer<State, Effect>) {
        delegatedContainer = container
    }

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