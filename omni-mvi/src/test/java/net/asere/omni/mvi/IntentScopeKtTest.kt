package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.asere.omni.core.EmptyCoroutineExceptionHandler
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

internal class IntentScopeKtTest : StateContainerHost<Any, Any> {

    override val container = mockk<InnerStateContainer<Any, Any>>(relaxed = true) {
        every { coroutineExceptionHandler } returns EmptyCoroutineExceptionHandler
        every { coroutineScope } returns CoroutineScope(EmptyCoroutineContext)
    }

    @Test
    fun `On postState invocation must call container update`() = runBlocking {
        val updateFunc: (Any) -> Unit = mockk()
        val intent: IntentScope<Any, Any>.() -> Unit = { postState(updateFunc) }
        intent(IntentScope(container))
        verify { container.update(updateFunc) }
    }

    @Test
    fun `On postEffect invocation must call container post`() = runBlocking {
        val effect: Any = mockk()
        val intent: IntentScope<Any, Any>.() -> Unit = { postEffect(effect) }
        intent(IntentScope(container))
        verify { container.post(effect) }
    }
}