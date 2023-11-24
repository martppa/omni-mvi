package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.coroutines.EmptyCoroutineContext

/**
 * This is a container decorator to allow testing a container behavior
 */
open class TestStateContainer<State, Effect> internal constructor(
    internal val collectionScope: CoroutineScope,
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), ExposedStateContainer<State, Effect>,
    StateContainerHost<State, Effect> {

    internal val emittedStates: MutableList<State> = Collections.synchronizedList(mutableListOf())
    internal val emittedEffects: MutableList<Effect> = Collections.synchronizedList(mutableListOf())

    init {
        collectStates()
        collectEffects()
    }

    private fun collectStates() = collectionScope.launch {
        state.toCollection(emittedStates)
    }

    private fun collectEffects() = collectionScope.launch {
        effect.toCollection(emittedEffects)
    }

    /**
     * Reset all recorded data
     */
    fun reset() {
        emittedStates.clear()
        emittedEffects.clear()
    }
}

internal fun <State, Effect> testStateContainer(
    collectionScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    container: ExposedStateContainer<State, Effect>
) = TestStateContainer(
    collectionScope = collectionScope,
    container = container,
)

/**
 * Turns this container into a test container
 */
fun <State, Effect> ExposedStateContainer<State, Effect>.buildTestContainer(
    collectionScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
) = testStateContainer(
    collectionScope = collectionScope,
    container = this
)

