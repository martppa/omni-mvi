package net.asere.omni.mvi

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

internal class InnerStateContainerHostKtTest : StateContainerHost<Any, Any> {

    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)
    private val block: () -> Unit = mockk()
    override val container: StateContainer<Any, Any> = stateContainer(
        initialState = Unit,
        coroutineScope = coroutineScope
    )

    @Test
    fun `On intent invocation must call block`() = runBlocking {
        intent { block() }
        verify { block() }
    }
}