package net.asere.omni.mvi

class DelegatorContainer<State, Effect, Action>(
    container: Container<State, Effect, Action>,
) : ContainerDecorator<State, Effect, Action>(
    container
), Container<State, Effect, Action> {

    private var delegatedContainer: StateContainer<State, Effect, Action>? = null

    fun delegate(container: StateContainer<State, Effect, Action>) {
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