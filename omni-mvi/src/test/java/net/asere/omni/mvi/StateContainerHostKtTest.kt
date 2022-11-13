package net.asere.omni.mvi

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class StateContainerHostKtTest {

    private val block: () -> Unit = mockk()
    private val stateContainerHost = object : StateContainerHost<Any, Any, Any> {
        override val container = stateContainer(initialState = Unit)
    }

    @Test
    fun `On intent invocation must call block`() = runBlocking {
        stateContainerHost.intent { block() }.join()
        verify { block() }
    }
}