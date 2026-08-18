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

class ClearPendingIntentKtTest : PendingContainerHost<Any, Any> {

    companion object {
        private const val STATIC_SEEK = "net.asere.omni.mvi.StateContainerDecoratorKt"
    }

    override val container: PendingContainer<Any, Any> = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(STATIC_SEEK)
        every { container.seek<PendingContainer<Any, Any>>(any()) } returns container
        unmockkStatic(STATIC_SEEK)
    }

    @Test
    fun `On clearPendingIntent container clearPendingIntent method should be called`(): Unit = runBlocking {
        clearPendingIntent()
        verify { container.clearPendingIntent(Unit) }
    }

    @Test
    fun `On clearPendingIntents container clearPendingIntents method should be called`(): Unit = runBlocking {
        clearPendingIntents()
        verify { container.clearPendingIntents() }
    }

    @Test
    fun `On launchPendingIntent container launchPendingIntent method should be called`(): Unit = runBlocking {
        launchPendingIntent()
        verify { container.launchPendingIntent(Unit) }
    }

    @Test
    fun `On launchPendingIntents container launchPendingIntents method should be called`(): Unit = runBlocking {
        launchPendingIntents()
        verify { container.launchPendingIntents() }
    }

    @After
    fun tearDown() {
        unmockkStatic(STATIC_SEEK)
    }
}
