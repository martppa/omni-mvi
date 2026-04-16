package net.asere.omni.mvi

/**
 * Data class containing the captured results of an MVI test execution.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property initialState The state of the container before the test started.
 * @property emittedStates A list of all states emitted during the test, in order.
 * @property emittedEffects A list of all effects emitted during the test, in order.
 * @property emittedElements A combined list of both states and effects in the exact order they were emitted.
 */
data class TestResult<State : Any, Effect : Any>(
    internal val initialState: State,
    internal val emittedStates: List<State>,
    internal val emittedEffects: List<Effect>,
    internal val emittedElements: List<EmittedElement>
)
