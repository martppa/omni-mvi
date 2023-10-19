package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

/**
 * Omni MVI basic Container
 */
interface Container {
    val coroutineScope: CoroutineScope
    val coroutineExceptionHandler: CoroutineExceptionHandler
}