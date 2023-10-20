package net.asere.omni.mvi

import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import net.asere.omni.core.EmptyCoroutineExceptionHandler
import org.junit.Assert.assertEquals
import org.junit.Test

internal class StateContainerBuilderKtTest : StateContainerHost<Any, Any> {

    private val initialState: Any = mockk()
    private val exceptionHandler = EmptyCoroutineExceptionHandler
    private val scope = CoroutineScope(exceptionHandler)

    override val container = stateContainer(
        initialState = initialState,
        coroutineScope = scope,
        coroutineExceptionHandler = exceptionHandler
    )

    @Test
    fun `On builder invocation must create a container with provided values`() {
        with(container) {
            assertEquals(initialState, container.asStateContainer().state.value)
            assertEquals(scope, coroutineScope)
            assertEquals(exceptionHandler, coroutineExceptionHandler)
        }
    }
}