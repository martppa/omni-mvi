package net.asere.omni.mvi

import java.util.Collections

/**
 * A [StateContainerDecorator] designed for recording emissions during unit tests.
 *
 * It intercepts all [update] and [post] calls, recording the resulting states and effects
 * into synchronized lists. This allow for asserting the behavior of an MVI host
 * in a multi-threaded test environment.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner container being tested.
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
     * Resets all recorded emissions.
     */
    fun reset() {
        emittedElements.synchronizedClear()
        emittedStates.synchronizedClear()
        emittedEffects.synchronizedClear()
    }

    /**
     * Updates the state and records the emission.
     */
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

    /**
     * Posts an effect and records the emission.
     */
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

/**
 * Internal factory function to create a [TestStateContainer].
 */
internal fun <State : Any, Effect : Any> testStateContainer(
    container: StateContainer<State, Effect>
) = TestStateContainer(container)

/**
 * Extension to wrap an existing [StateContainer] into a [TestStateContainer].
 *
 * @return A new [TestStateContainer] instance decorating the original one.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.buildTestContainer() =
    testStateContainer(this)

/**
 * Thread-safe addition to a [MutableList].
 */
fun <T> MutableList<T>.synchronizedAdd(element: T) = synchronized(this) {
    add(element)
}

/**
 * Thread-safe clear for a [MutableList].
 */
fun <T> MutableList<T>.synchronizedClear() = synchronized(this) {
    clear()
}
