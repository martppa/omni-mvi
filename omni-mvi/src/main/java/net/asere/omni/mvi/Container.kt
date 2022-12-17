package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

interface Container<State, Effect> {
    val coroutineScope: CoroutineScope
    val coroutineExceptionHandler: CoroutineExceptionHandler
}