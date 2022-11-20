package net.asere.omni.mvi

import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test

internal class StateContainerBuilderKtTest : StateContainerHost<Any, Any, Any> {

    private val state: Any = mockk()
    private val scope: CoroutineScope = mockk()
    private val exceptionHandler: CoroutineExceptionHandler = mockk()
    private val action: (Any) -> Unit = mockk()

    override val container = stateContainer(
        initialState = state,
        onAction = action,
        coroutineScope = scope,
        coroutineExceptionHandler = exceptionHandler
    )

    @Test
    fun `On builder invocation must create a container with provided values`() {
        with(container) {
            assertEquals(state, state.value)
            assertEquals(action, onAction)
            assertEquals(scope, coroutineScope)
            assertEquals(exceptionHandler, coroutineExceptionHandler)
        }
    }
}