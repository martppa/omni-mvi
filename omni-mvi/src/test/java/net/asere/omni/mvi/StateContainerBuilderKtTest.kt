package net.asere.omni.mvi

import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test

internal class StateContainerBuilderKtTest {

    @Test
    fun `On builder invocation must create a container with provided values`() {
        val state: Any = mockk()
        val scope: CoroutineScope = mockk()
        val exceptionHandler: CoroutineExceptionHandler = mockk()
        val action: (Any) -> Unit = mockk()
        val containerHost = object : StateContainerHost<Any, Any, Any> {
            override val container: Container<Any, Any, Any> = stateContainer(
                initialState = state,
                onAction = action,
                coroutineScope = scope,
                coroutineExceptionHandler = exceptionHandler
            )
        }

        with(containerHost.container as StateContainer) {
            assertEquals(state, uiState.value)
            assertEquals(action, onAction)
            assertEquals(scope, coroutineScope)
            assertEquals(exceptionHandler, coroutineExceptionHandler)
        }
    }
}