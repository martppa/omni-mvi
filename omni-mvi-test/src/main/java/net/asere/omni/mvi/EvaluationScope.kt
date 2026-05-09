package net.asere.omni.mvi

import org.junit.Assert

/**
 * A scope for evaluating and asserting the results of an MVI test.
 *
 * This class provides a fluent API to verify the sequence of states and effects
 * that were emitted during a test. It uses internal iterators to ensure that
 * assertions are made in the exact order the emissions occurred.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property testResult The captured results to be evaluated.
 */
class EvaluationScope<State : Any, Effect : Any>(
    private val testResult: TestResult<State, Effect>
) {
    internal val stateIterator = testResult.emittedStates.listIterator()
    internal val effectIterator = testResult.emittedEffects.listIterator()
    private val elementIterator = testResult.emittedElements.listIterator()

    /**
     * The initial state of the container before any actions were processed.
     */
    val initialState = testResult.initialState

    /**
     * All states captured during the test.
     */
    val emittedStates = testResult.emittedStates

    /**
     * All effects captured during the test.
     */
    val emittedEffects = testResult.emittedEffects

    /**
     * Asserts that the next emission is a [State] and provides it for verification.
     *
     * @param block A lambda that receives the previous state and the current (next) state.
     * @throws IllegalStateException if no more states are available or if the next emission is an effect.
     */
    fun nextState(block: (previous: State, current: State) -> Unit) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${testResult.emittedStates.size} states were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.State)
                throw IllegalStateException("A state was expected but an effect " +
                    "was emitted at position ${elementIterator.previousIndex()}. " +
                    "The emitted effect ${this.element} at position " +
                    "${elementIterator.previousIndex()} is not the next state")
        }

        if (!stateIterator.hasPrevious()) {
            block(testResult.initialState, stateIterator.next())
        } else {
            block(testResult.emittedStates[stateIterator.previousIndex()], stateIterator.next())
        }
    }

    /**
     * Asserts that the next emission is a [State] and that it matches the result of applying [block]
     * to the previous state.
     *
     * @param block A lambda that describes the expected state transition.
     * @throws IllegalStateException if no more states are available or if the next emission is an effect.
     */
    fun expectState(block: State.() -> State) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedStates.size} states were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.State)
                throw IllegalStateException("A state was expected but an effect " +
                    "was emitted at position ${elementIterator.previousIndex()}. " +
                    "The emitted effect ${this.element} at position " +
                    "${elementIterator.previousIndex()} is not the next state")
        }

        val expected = if (!stateIterator.hasPrevious()) {
            testResult.initialState.block()
        } else {
            emittedStates[stateIterator.previousIndex()].block()
        }
        Assert.assertEquals(expected, stateIterator.next())
    }

    /**
     * Asserts that the next emission is an [Effect] and provides it for verification.
     *
     * @param block A lambda that receives the emitted effect.
     * @throws IllegalStateException if no more effects are available or if the next emission is a state.
     */
    fun nextEffect(block: (Effect) -> Unit) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.Effect)
                throw IllegalStateException("An effect was expected but a state " +
                    "was emitted at position ${elementIterator.previousIndex()}. " +
                    "The emitted ${this.element} at position " +
                    "${elementIterator.previousIndex()} is not the expected effect")
        }

        block(effectIterator.next())
    }

    /**
     * Asserts that the next emission is an [Effect] and that it matches the provided [effect].
     *
     * @param effect The expected side effect.
     * @throws IllegalStateException if no more effects are available or if the next emission is a state.
     */
    fun expectEffect(effect: Effect) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.Effect)
                throw IllegalStateException(
                    "An effect was expected but a state " +
                            "was emitted at position ${elementIterator.previousIndex()}. " +
                            "The emitted ${this.element} at position " +
                            "${elementIterator.previousIndex()} is not the expected effect"
                )
        }

        Assert.assertEquals(effect, effectIterator.next())
    }
}
