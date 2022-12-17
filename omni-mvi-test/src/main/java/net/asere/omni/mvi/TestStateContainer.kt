package net.asere.omni.mvi

open class TestStateContainer<State, Effect> internal constructor(
    override val container: Container<State, Effect>,
) : ContainerDecorator<State, Effect>(
    container
), Container<State, Effect>,
    StateContainerHost<State, Effect> {

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