package net.asere.omni.mvi

/**
 * Test result data
 */
data class TestResult<State : Any, Effect : Any>(
    internal val initialState: State,
    internal val emittedStates: List<State>,
    internal val emittedEffects: List<Effect>,
    internal val emittedElements: List<EmittedElement>
)
