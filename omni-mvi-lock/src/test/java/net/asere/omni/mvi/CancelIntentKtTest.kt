package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class CancelIntentKtTest : LockContainerHost<Any, Any> {

    companion object {
        private const val STATIC_SEEK = "net.asere.omni.mvi.StateContainerDecoratorKt"
    }

    override val container: LockContainer<Any, Any> = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(STATIC_SEEK)
        every { container.seek<LockContainer<Any, Any>>(any()) } returns container
        unmockkStatic(STATIC_SEEK)
    }

    @Test
    fun `On intent cancellation container cancel method should be called`(): Unit = runBlocking {
        cancelIntent()
        verify { container.cancelIntent(Unit) }
    }

    @After
    fun tearDown() {
        unmockkStatic(STATIC_SEEK)
    }
}