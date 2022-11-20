package net.asere.omni.mvi

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import org.junit.Assert.*
import org.junit.Test

class LockableIntentTest {

    @Test
    fun `On check if an intent is locked when active and not locked must return true`() {
        val job: Job = mockk(relaxed = true) {
            every { isActive } returns true
        }
        val intent = LockableIntent(job, locked = false)
        assertTrue(intent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when not active and not locked must return false`() {
        val job: Job = mockk(relaxed = true) {
            every { isActive } returns false
        }
        val intent = LockableIntent(job, locked = false)
        assertTrue(!intent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when active and locked must return true`() {
        val job: Job = mockk(relaxed = true) {
            every { isActive } returns true
        }
        val intent = LockableIntent(job, locked = true)
        assertTrue(intent.isLocked())
    }

    @Test
    fun `On check if an intent is locked when not active and locked must return true`() {
        val job: Job = mockk(relaxed = true) {
            every { isActive } returns false
        }
        val intent = LockableIntent(job, locked = true)
        assertTrue(intent.isLocked())
    }
}