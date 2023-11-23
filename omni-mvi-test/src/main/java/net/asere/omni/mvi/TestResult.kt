package net.asere.omni.mvi

import org.junit.Assert

/**
 * Test result data
 */
class TestResult<State, Effect>(
    private val initialState: State,
    val emittedStates: List<State>,
    val emittedEffects: List<Effect>
) {
    internal val stateIterator = emittedStates.listIterator()
    internal val effectIterator = emittedEffects.listIterator()

    fun nextState(block: (previous: State, current: State) -> Unit) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedStates.size} states were emitted.")
        if (!stateIterator.hasPrevious()) {
            block(initialState, stateIterator.next())
        } else {
            block(emittedStates[stateIterator.previousIndex()], stateIterator.next())
        }
    }

    fun expectState(block: State.() -> State) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedStates.size} states were emitted.")
        val expected = if (!stateIterator.hasPrevious()) {
            initialState.block()
        } else {
            emittedStates[stateIterator.previousIndex()].block()
        }
        Assert.assertEquals(expected, stateIterator.next())
    }

    fun nextEffect(block: (Effect) -> Unit) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")
        block(effectIterator.next())
    }

    fun expectEffect(effect: Effect) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")
        Assert.assertEquals(effect, effectIterator.next())
    }
}
