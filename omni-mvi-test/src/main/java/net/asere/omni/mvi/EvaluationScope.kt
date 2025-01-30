package net.asere.omni.mvi

import org.junit.Assert

class EvaluationScope<State : Any, Effect : Any>(
    private val testResult: TestResult<State, Effect>
) {
    internal val stateIterator = testResult.emittedStates.listIterator()
    internal val effectIterator = testResult.emittedEffects.listIterator()
    private val elementIterator = testResult.emittedElements.listIterator()
    val emittedStates = testResult.emittedStates
    val emittedEffects = testResult.emittedEffects

    fun nextState(block: (previous: State, current: State) -> Unit) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${testResult.emittedStates.size} states were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.State)
                throw IllegalStateException("Emitted ${this.element} at position " +
                        "${elementIterator.previousIndex()} is not the next state")
        }

        if (!stateIterator.hasPrevious()) {
            block(testResult.initialState, stateIterator.next())
        } else {
            block(testResult.emittedStates[stateIterator.previousIndex()], stateIterator.next())
        }
    }

    fun expectState(block: State.() -> State) {
        if (!stateIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedStates.size} states were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.State)
                throw IllegalStateException("Emitted ${this.element} at position " +
                        "${elementIterator.previousIndex()} is not the expected state")
        }

        val expected = if (!stateIterator.hasPrevious()) {
            testResult.initialState.block()
        } else {
            emittedStates[stateIterator.previousIndex()].block()
        }
        Assert.assertEquals(expected, stateIterator.next())
    }

    fun nextEffect(block: (Effect) -> Unit) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.Effect)
                throw IllegalStateException("Emitted ${this.element} at position " +
                        "${elementIterator.previousIndex()} is not the next effect")
        }

        block(effectIterator.next())
    }

    fun expectEffect(effect: Effect) {
        if (!effectIterator.hasNext())
            throw IllegalStateException("A maximum of ${emittedEffects.size} effects were emitted.")

        elementIterator.next().apply {
            if (this.type != EmittedElement.Type.Effect)
                throw IllegalStateException("Emitted ${this.element} at position " +
                        "${elementIterator.previousIndex()} is not the expected effect")
        }

        Assert.assertEquals(effect, effectIterator.next())
    }
}