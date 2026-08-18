package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class LockableIntentTest {

    @Test
    fun `On check if an intent is locked when active and not locked must return true`() {
        val intent: Intent = mockk(relaxed = true) {
            every { isActive } returns true
        }
        val lockableIntent = LockableIntent(intent, locked = false)
        assertTrue(lockableIntent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when not active and not locked must return false`() {
        val intent: Intent = mockk(relaxed = true) {
            every { isActive } returns false
        }
        val lockableIntent = LockableIntent(intent, locked = false)
        assertTrue(!lockableIntent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when active and locked must return true`() {
        val intent: Intent = mockk(relaxed = true) {
            every { isActive } returns true
        }
        val lockableIntent = LockableIntent(intent, locked = true)
        assertTrue(lockableIntent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when not active and locked must return true`() {
        val intent: Intent = mockk(relaxed = true) {
            every { isActive } returns false
        }
        val lockableIntent = LockableIntent(intent, locked = true)
        assertTrue(lockableIntent.isLocked())
    }
}
