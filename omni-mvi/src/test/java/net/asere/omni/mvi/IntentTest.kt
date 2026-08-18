package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class IntentTest {

    @Test
    fun `On default instantiation id should be a UUID and job should delegate`() {
        val job = Job()
        val intent = Intent(job)

        assertNotNull(intent.id)
        assertTrue(intent.id is UUID)
        assertEquals(CoroutineStart.DEFAULT, intent.start)
        assertTrue(intent.isActive)
    }

    @Test
    fun `On custom parameters instantiation properties should match provided values`() {
        val job = Job()
        val customId = "CUSTOM_INTENT_ID"
        val start = CoroutineStart.LAZY
        val intent = Intent(job = job, id = customId, start = start)

        assertEquals(customId, intent.id)
        assertEquals(start, intent.start)
    }

    @Test
    fun `On cancelling intent underlying job should be cancelled`() {
        val job = Job()
        val intent = Intent(job)

        intent.cancel()

        assertTrue(intent.isCancelled)
        assertTrue(job.isCancelled)
    }

    @Test
    fun `On equals and hashCode matching properties should be equal`() {
        val job = Job()
        val id = "TEST_ID"
        val start = CoroutineStart.DEFAULT
        val intent1 = Intent(job, id, start)
        val intent2 = Intent(job, id, start)
        val intent3 = Intent(Job(), id, start)

        assertEquals(intent1, intent2)
        assertEquals(intent1.hashCode(), intent2.hashCode())
        assertNotEquals(intent1, intent3)
    }
}
