package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.Assert.assertEquals

internal class CurrentStateKtTest : StateContainerHost<Any, Any, Any> {

    private val fakeState: Any = mockk()
    override val container: StateContainer<Any, Any, Any> = mockk {
        every { state } returns MutableStateFlow(fakeState)
    }

    @Test
    fun `On currentState reference must return state at container`() {
        val currentState = currentState
        assertEquals(currentState, fakeState)
    }
}