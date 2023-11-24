package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer
import java.lang.IllegalArgumentException
import kotlin.coroutines.EmptyCoroutineContext

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
    testScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    val initialState = from ?: container.asStateContainer().initialState

    if (take != null && take.count <= 0)
        throw IllegalArgumentException("Take argument count should be grater than 0 if set")

    val testContainer = container.buildTestContainer(testScope)

    await()
    testContainer.reset()
    container.asStateContainer().update { initialState }

    testBlock()

    with(testContainer) {
        take?.let {
            container.deepAwait {
                (it is TakeStates && it.count == emittedStates.size + 1) ||
                        (it is TakeEffects && it.count == emittedEffects.size)
            }
        } ?: await()
        TestResult(
            initialState = from ?: initialState,
            emittedStates = emittedStates.apply { emittedStates.removeFirst() },
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
    testScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()
    val initialState = host.currentState

    val testContainer = host.container.buildTestContainer(testScope)
    testContainer.await()

    with(testContainer) {
        TestResult(
            initialState = initialState,
            emittedStates = emittedStates.apply { removeFirst() },
            emittedEffects = emittedEffects,
        )
    }
}

private val <State, Effect> StateContainer<State, Effect>.initialState
    get() = seek<StateContainer<State, Effect>> {
        it is StateContainer<*, *>
    }.initialState