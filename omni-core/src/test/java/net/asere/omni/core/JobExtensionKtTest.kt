package net.asere.omni.core

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class JobExtensionKtTest {

    @Test
    fun `On recursive join will recursively seek and join nested children`() = runTest {
        val mutex = Mutex()
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        var jobCounter = 0
        val job = coroutineScope.launch {
            mutex.withLock { jobCounter++ }
            launch { delay(200); mutex.withLock { jobCounter++ } }
            launch {
                delay(100);
                mutex.withLock { jobCounter++ }
                launch { delay(30); mutex.withLock { jobCounter++ } }
                launch {
                    delay(100);
                    launch {
                        delay(300);
                        mutex.withLock { jobCounter++ }
                    }
                }
            }
            launch { delay(100); mutex.withLock { jobCounter++ } }
            launch { delay(400); mutex.withLock { jobCounter++ } }
        }
        job.recursiveJoinChildren()
        job.join()
        assertEquals(7, jobCounter)
    }

    @Test
    fun `On join children will join children`() = runTest {
        val mutex = Mutex()
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        var jobCounter = 0
        val job = coroutineScope.launch {
            launch { delay(200); mutex.withLock { jobCounter++ } }
            launch { delay(100); mutex.withLock { jobCounter++ } }
            launch { delay(50); mutex.withLock { jobCounter++ } }
            launch { delay(10); mutex.withLock { jobCounter++ } }
        }
        job.joinChildren()
        job.join()
        assertEquals(4, jobCounter)
    }
}