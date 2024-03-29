package net.asere.omni.mvi

import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer
import java.lang.IllegalArgumentException

/**
 * Puts the container host in evaluation status and offers a scope of with testing data such as
 * emitted states and emitted effects.
 *
 * @param relax Set true if you want to avoid iterating over all emitted states and effects
 * @param block Evaluation code block
 */
fun <State, Effect> TestResult<State, Effect>.evaluate(
    relax: Boolean = false,
    block: TestResult<State, Effect>.() -> Unit
) {
    this.block()
    if (!relax) {
        if (effectIterator.hasNext())
            throw IllegalStateException("${effectIterator.nextIndex()} effects were tested but ${emittedEffects.size} were emitted.")
        if (stateIterator.hasNext())
            throw IllegalStateException("${stateIterator.nextIndex()} states were tested but ${emittedStates.size} were emitted.")
    }
}

/**
 * Call this extension function to start testing an action
 *
 * @param action Action to test
 * @param take argument to define how many data items must be collected
 */
suspend fun <State, Effect, Action> ActionContainerHost<State, Effect, Action>.testOn(
    action: Action,
    take: Take? = null,
    withState: State? = null,
) = testIntent(
    from = withState,
    take = take
) { on(action) }

/**
 * Call this method to start testing an intent
 *
 * @param testBlock intent reference or testing content
 * @param take argument to define how many data items must be collected
 */
suspend fun <State, Effect, Host : StateContainerHost<State, Effect>> Host.testIntent(
    take: Take? = null,
    from: State? = null,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    val initialState = from ?: container.asStateContainer().initialState

    if (take != null && take.count <= 0)
        throw IllegalArgumentException("take argument count should be grater than 0 if set")

    val testContainer = container.buildTestContainer().also { delegate(it) }

    await()
    container.asStateContainer().update { initialState }
    testContainer.reset()

    testBlock()

    with(testContainer) {
        await {
            take is TakeStates && take.count == emittedStates.size ||
                    take is TakeEffects && take.count == emittedEffects.size
        }
        TestResult(
            initialState = from ?: initialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
        )
    }
}

/**
 * Test a host constructor
 *
 * @param builder Host construction builder function
 */
suspend fun <State, Effect> testConstructor(
    initialState: State? = null,
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()

    val chosenInitialState = initialState ?: host.currentState
    initialState?.let { host.container.asStateContainer().update { it } }
    val testContainer = host.container.buildTestContainer().also { host.delegate(it) }

    host.await()

    with(testContainer) {
        TestResult(
            initialState = chosenInitialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
        )
    }
}