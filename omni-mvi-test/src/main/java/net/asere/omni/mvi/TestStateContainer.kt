package net.asere.omni.mvi

import java.util.Collections

/**
 * This is a container decorator to allow testing a container behavior
 */
open class TestStateContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect>,
    StateContainerHost<State, Effect> {

    internal val emittedStates = Collections.synchronizedList(mutableListOf<State>())
    internal val emittedEffects = Collections.synchronizedList(mutableListOf<Effect>())
    internal val emittedElements = Collections.synchronizedList(mutableListOf<EmittedElement>())

    /**
     * Reset all recorded data
     */
    fun reset() {
        emittedElements.synchronizedClear()
        emittedStates.synchronizedClear()
        emittedEffects.synchronizedClear()
    }

    override fun update(function: State.() -> State) {
        val updatedState = currentState.function()
        emittedStates.synchronizedAdd(updatedState)
        emittedElements.synchronizedAdd(
            EmittedElement(
                type = EmittedElement.Type.State,
                element = updatedState
            )
        )
    }

    override fun post(effect: Effect) {
        emittedEffects.synchronizedAdd(effect)
        emittedElements.synchronizedAdd(
            EmittedElement(
                type = EmittedElement.Type.Effect,
                element = effect
            )
        )
    }
}

internal fun <State : Any, Effect : Any> testStateContainer(
    container: StateContainer<State, Effect>
) = TestStateContainer(container)

/**
 * Turns this container into a test container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.buildTestContainer() =
    testStateContainer(this)

fun <T> MutableList<T>.synchronizedAdd(element: T) = synchronized(this) { add(element) }
fun <T> MutableList<T>.synchronizedClear() = synchronized(this) { clear() }

