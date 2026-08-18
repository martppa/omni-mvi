package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import java.util.UUID

/**
 * Represents an asynchronous MVI intent execution.
 *
 * An [Intent] delegates to an underlying coroutine [Job], providing full control
 * over its lifecycle (e.g., [cancel], [join], [isActive]), while also holding a non-null [id]
 * and its [start] policy.
 */
@OptIn(InternalCoroutinesApi::class)
interface Intent : Job {
    /**
     * An identifier for the intent.
     */
    val id: Any

    /**
     * The [CoroutineStart] policy used to launch this intent.
     */
    val start: CoroutineStart
}

/**
 * Creates a new [Intent] instance.
 *
 * @param job The underlying coroutine [Job] running this intent.
 * @param id An identifier for the intent (defaults to a random [UUID]).
 * @param start The [CoroutineStart] policy (defaults to [CoroutineStart.DEFAULT]).
 * @return A new [Intent] instance.
 */
fun Intent(
    job: Job,
    id: Any = UUID.randomUUID(),
    start: CoroutineStart = CoroutineStart.DEFAULT
): Intent = IntentImpl(job, id, start)

@OptIn(InternalCoroutinesApi::class)
private class IntentImpl(
    private val job: Job,
    override val id: Any,
    override val start: CoroutineStart
) : Intent, Job by job {

    override fun toString(): String {
        return "Intent(id=$id, start=$start, job=$job)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Intent) return false
        return job == (other as? IntentImpl)?.job && id == other.id && start == other.start
    }

    override fun hashCode(): Int {
        var result = job.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + start.hashCode()
        return result
    }
}
