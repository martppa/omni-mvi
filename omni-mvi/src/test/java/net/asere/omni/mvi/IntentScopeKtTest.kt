package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

internal class IntentScopeKtTest : StateContainerHost<Any, Any> {

    private val errorBlock: (Throwable) -> Unit = mockk(relaxed = true)
    override val container = mockk<StateContainer<Any, Any>>(relaxed = true) {
        every { coroutineExceptionHandler } returns EmptyCoroutineExceptionHandler
        every { coroutineScope } returns CoroutineScope(EmptyCoroutineContext)
    }

    @Test
    fun `On failing intent invocation must call onError`() = runBlocking {
        val scope = IntentScope(container, errorBlock)
        val errorLambda: (Throwable) -> Unit = mockk()
        val intent: IntentScope<Any, Any>.() -> Unit = { onError(errorLambda) }
        intent(scope)
        Assert.assertEquals(scope.errorBlock, errorLambda)
    }

    @Test
    fun `On postState invocation must call container update`() = runBlocking {
        val updateFunc: (Any) -> Unit = mockk()
        val intent: IntentScope<Any, Any>.() -> Unit = { postState(updateFunc) }
        intent(IntentScope(container, errorBlock))
        verify { container.update(updateFunc) }
    }

    @Test
    fun `On postEffect invocation must call container post`() = runBlocking {
        val effect: Any = mockk()
        val intent: IntentScope<Any, Any>.() -> Unit = { postEffect(effect) }
        intent(IntentScope(container, errorBlock))
        verify { container.post(effect) }
    }
}