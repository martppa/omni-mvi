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

internal class StateContainerHostKtTest : StateContainerHost<Any, Any> {

    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)
    private val block: () -> Unit = mockk()
    override val container: Container<Any, Any> = stateContainer(
        initialState = Unit,
        coroutineScope = coroutineScope
    )

    @Test
    fun `On intent invocation must call block`() = runBlocking {
        intent { block() }.join()
        verify { block() }
    }

    @Test
    fun `On intent invocation must not happen with empty coroutine`() = runBlocking {
        mockkStatic(coroutineScope::launch)
        every {
            coroutineScope.launch(
                block = any(),
                context = any(),
                start = any()
            )
        } answers { launch { /* Empty body */ } }
        intent { block() }.join()
        verify(exactly = 0) { block() }
        unmockkStatic(coroutineScope::launch)
    }
}