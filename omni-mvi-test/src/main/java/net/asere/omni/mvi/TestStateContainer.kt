package net.asere.omni.mvi

/**
 * This is a container decorator to allow testing a container behavior
 */
open class TestStateContainer<State, Effect> internal constructor(
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), ExposedStateContainer<State, Effect>,
    StateContainerHost<State, Effect> {

    internal val emittedStates: MutableList<State> = mutableListOf()
    internal val emittedEffects: MutableList<Effect> = mutableListOf()

    /**
     * Reset all recorded data
     */
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