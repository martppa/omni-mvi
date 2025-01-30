package net.asere.omni.mvi

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * This is a container decorator to allow testing a container behavior
 */
open class TestStateContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect>,
    StateContainerHost<State, Effect> {

    private val mutex = Mutex()

    internal val emittedStates: MutableList<State> = mutableListOf()
    internal val emittedEffects: MutableList<Effect> = mutableListOf()
    internal val emittedElements: MutableList<EmittedElement> = mutableListOf()

    /**
     * Reset all recorded data
     */
    fun reset() {
        emittedElements.clear()
        emittedStates.clear()
        emittedEffects.clear()
    }

    override fun update(function: State.() -> State) {
        val updatedState = currentState.function()
        emittedStates.add(updatedState)
        container.coroutineScope.launch {
            mutex.withLock {
                emittedElements.add(
                    EmittedElement(
                        type = EmittedElement.Type.State,
                        element = updatedState
                    )
                )
            }
        }
    }

    override fun post(effect: Effect) {
        emittedEffects.add(effect)
        container.coroutineScope.launch {
            mutex.withLock {
                emittedElements.add(
                    EmittedElement(
                        type = EmittedElement.Type.Effect,
                        element = effect
                    )
                )
            }
        }
    }
}

internal fun <State : Any, Effect : Any> testStateContainer(
    container: StateContainer<State, Effect>
) = TestStateContainer(container)

/**
 * Turns this container into a test container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>
        .buildTestContainer() = testStateContainer(this)

