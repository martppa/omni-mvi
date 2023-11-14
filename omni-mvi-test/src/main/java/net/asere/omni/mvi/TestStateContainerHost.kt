package net.asere.omni.mvi

import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer
import java.lang.IllegalArgumentException

/**
 * Puts the container host in evaluation status and offers a scope of with testing data such as
 * emitted states and emitted effects.
 */
fun <State, Effect> TestResult<State, Effect>.evaluate(block: TestResult<State, Effect>.() -> Unit) =
    this.block()

/**
 * Test result data
 */
data class TestResult<State, Effect>(
    val emittedStates: List<State>,
    val emittedEffects: List<Effect>
)

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
    withState = withState,
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
    withState: State? = null,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    if (take != null && take.count <= 0)
        throw IllegalArgumentException("take argument count should be grater than 0 if set")
    val testContainer = container.buildTestContainer()
    delegate(testContainer)
    awaitJobs()
    withState?.let { testContainer.update { it } }
    testContainer.reset()
    testBlock()
    with(testContainer) {
        awaitJobs {
            take is TakeStates && take.count == emittedStates.size ||
                    take is TakeEffects && take.count == emittedEffects.size
        }
        TestResult(emittedStates, emittedEffects)
    }
}

/**
 * Test a host constructor
 *
 * @param builder Host construction builder function
 */
suspend fun <State, Effect> testConstructor(
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()
    val testContainer = TestStateContainer(host.container)
    host.delegate(testContainer)
    host.awaitJobs()
    with(testContainer) { TestResult(emittedStates, emittedEffects) }
}