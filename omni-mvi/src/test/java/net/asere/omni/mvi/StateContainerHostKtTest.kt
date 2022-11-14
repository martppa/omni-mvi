package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class StateContainerHostKtTest {

    private val block: () -> Unit = mockk()

    @Test
    fun `On intent invocation must call block`() = runBlocking {
        val host = stateContainerHost<Any, Any, Any>(initialState = Unit)
        host.intent { block() }.join()
        verify { block() }
    }

    @Test
    fun `On intent invocation must not happen with empty coroutine`() = runBlocking {
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        mockkStatic(coroutineScope::launch)
        every {
            coroutineScope.launch(
                block = any(),
                context = any(),
                start = any()
            )
        } answers { launch { /* Empty body */ } }
        stateContainerHost<Any, Any, Any>(
            initialState = Unit,
            coroutineScope = coroutineScope
        ).intent { block() }.join()
        verify(exactly = 0) { block() }
        unmockkStatic(coroutineScope::launch)
    }
}