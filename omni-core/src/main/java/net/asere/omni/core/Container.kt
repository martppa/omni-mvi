package net.asere.omni.core

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

/**
 * Base interface for an Omni MVI Container.
 *
 * A Container provides the fundamental coroutine infrastructure required for
 * MVI operations, including a [CoroutineScope] for managing lifecycle and
 * a [CoroutineExceptionHandler] for centralized error management.
 */
interface Container {
    /**
     * The [CoroutineScope] in which all container-related operations are executed.
     */
    val coroutineScope: CoroutineScope

    /**
     * The [CoroutineExceptionHandler] used to handle uncaught exceptions within the [coroutineScope].
     */
    val coroutineExceptionHandler: CoroutineExceptionHandler
}
