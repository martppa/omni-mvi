package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class IntentScopeKtTest : StateContainerHost<Any, Any, Any> {

    private val errorBlock: (Throwable) -> Unit = mockk(relaxed = true)
    override val container: StateContainer<Any, Any, Any> = mockk(relaxed = true) {
        every { coroutineExceptionHandler } returns EmptyCoroutineExceptionHandler
        every { coroutineScope } returns CoroutineScope(EmptyCoroutineContext)
    }

    @Before
    fun setup() {
        mockkConstructor(IntentScope::class)
        every { anyConstructed<IntentScope<Any, Any>>().errorBlock } returns errorBlock
    }

    @Test
    fun `On failing intent invocation must call onError`() = runBlocking {
        val exception: RuntimeException = mockk()
        intent { throw exception }.join()
        verify { errorBlock.invoke(exception) }
    }

    @Test
    fun `On postState invocation must call container update`() = runBlocking {
        val updateFunc: (Any) -> Unit = mockk()
        intent { postState(updateFunc) }.join()
        verify { container.update(updateFunc) }
    }

    @Test
    fun `On postEffect invocation must call container update`() = runBlocking {
        val effect: Any = mockk()
        intent { postEffect(effect) }.join()
        verify { container.post(effect) }
    }

    @After
    fun drop() {
        unmockkConstructor(IntentScope::class)
    }
}