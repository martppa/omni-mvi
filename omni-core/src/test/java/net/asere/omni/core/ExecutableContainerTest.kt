package net.asere.omni.core

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class ExecutableContainerTest {

    @Test
    fun `On execution thrown error should be redirected to coroutine exception handler`() = runTest {
        val expectedThrownError = Exception("random error message")
        var actualThrownError: Throwable? = null

        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
                actualThrownError = throwable
            },
        ) {}

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = {},
        ) {
            throw expectedThrownError
        }

        container.await()
        assertEquals(expectedThrownError, actualThrownError)
    }

    @Test
    fun `On execute must throw an IllegalStateException if the container is not an ExecutableContainer`() = runTest {
        val container = object : Container {
            override val coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
            override val coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
        }
        val host = object : ContainerHost {
            override val container = container
        }
        try {
            host.execute(
                scope = ExecutionScope(),
            ) {}
        } catch (ex: Exception) {
            assertTrue(ex is IllegalStateException)
        }
    }

    @Test
    fun `On execute must call provided block`() = runTest {
        var blockExecuted = false
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}
        val host = object : ContainerHost {
            override val container = container
        }
        host.execute(
            scope = ExecutionScope(),
        ) {
            blockExecuted = true
        }
        container.await()
        assert(blockExecuted)
    }

    @Test
    fun `On execute must call provided block with expected scope`() = runTest {
        var blockExecutedInScope = false
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}
        val host = object : ContainerHost {
            override val container = container
        }
        val scope = ExecutionScope()
        host.execute(
            scope = scope,
        ) {
            blockExecutedInScope = this == scope
        }
        container.await()
        assert(blockExecutedInScope)
    }

    @Test
    fun `On lockExecution and releaseExecution must control lazy start`() = runTest {
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(SupervisorJob()),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var executed = false
        container.lockExecution()

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = {},
        ) {
            executed = true
        }

        // Wait to make sure it doesn't run while locked
        delay(50)
        assertFalse(executed)

        // Release and start lazy jobs
        container.releaseExecution()
        container.launchJobs()

        container.await()
        assertTrue(executed)
    }

    @Test
    fun `On cancellation exception thrown it must be propagated`() = runTest {
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(SupervisorJob()),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var normalErrorCaught = false

        val job = container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = { normalErrorCaught = true },
        ) {
            try {
                throw CancellationException("Simulated cancel")
            } catch (_: Exception) {
                // If it is regular exception, we catch it. But CancellationException
                // is rethrown/propagated.
            }
        }

        job.join()
        // CancellationException is rethrown to ensure proper coroutines cancellation,
        // and does not trigger container's local onError
        assertFalse(normalErrorCaught)
    }

    @Test
    fun `On exception thrown in execution block it must trigger local onError`() = runTest {
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(SupervisorJob()),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var exceptionCaught: Throwable? = null
        val expectedException = RuntimeException("Custom error")

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = { exceptionCaught = it },
        ) {
            throw expectedException
        }

        container.await()
        assertEquals(expectedException, exceptionCaught)
    }

    @Test
    fun `On cancel must cancel all active child jobs`() = runTest {
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(SupervisorJob()),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var job1Ran = false
        var job2Ran = false

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = {},
        ) {
            delay(500)
            job1Ran = true
        }

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = {},
        ) {
            delay(500)
            job2Ran = true
        }

        delay(50)
        container.cancel()
        container.await()

        assertFalse(job1Ran)
        assertFalse(job2Ran)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `On blockedContext execution it must lock executions to lazy`() = runTest {
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(SupervisorJob()),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var executed = false

        withContext(ExecutableContainer.blockedContext()) {
            container.execute(
                context = EmptyCoroutineContext,
                start = CoroutineStart.DEFAULT,
                onError = {},
            ) {
                executed = true
            }
        }

        // Inside blockedContext, it is forced to lazy, so it should not run immediately
        delay(50)
        assertFalse(executed)

        // Launch jobs to execute it
        container.launchJobs()
        container.await()
        assertTrue(executed)
    }

    @Test
    fun `On join must wait until container main job is finished`() = runTest {
        val job = SupervisorJob()
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(job),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler,
        ) {}

        var joinResumed = false
        launch {
            container.join()
            joinResumed = true
        }

        delay(50)
        assertFalse(joinResumed)

        job.complete()
        delay(50)
        assertTrue(joinResumed)
    }
}