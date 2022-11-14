package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.Assert.assertEquals

internal class CurrentStateKtTest {

    private val fakeState: Any = mockk()
    private val stateContainer: StateContainer<Any, Any, Any> = mockk {
        every { uiState } returns MutableStateFlow(fakeState)
    }
    private val stateContainerHost = stateContainerHost(stateContainer)

    @Test
    fun `On currentState reference must return state at container`() {
        val currentState = stateContainerHost.currentState
        assertEquals(currentState, fakeState)
    }
}