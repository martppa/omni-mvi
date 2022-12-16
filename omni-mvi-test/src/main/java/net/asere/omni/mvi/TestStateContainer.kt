package net.asere.omni.mvi

open class TestStateContainer<State, Effect, Action> internal constructor(
    override val container: Container<State, Effect, Action>,
) : ContainerDecorator<State, Effect, Action>(
    container
), Container<State, Effect, Action>,
    StateContainerHost<State, Effect, Action> {

    internal val emittedStates: MutableList<State> = mutableListOf()
    internal val emittedEffects: MutableList<Effect> = mutableListOf()

    fun reset() {
        emittedStates.clear()
        emittedEffects.clear()
    }

    override fun update(function: State.() -> State) {
        val updatedState = currentState.function()
        emittedStates.add(updatedState)
    }

    override fun post(effect: Effect) {
        emittedEffects.add(effect)
    }
}