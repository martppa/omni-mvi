package net.asere.omni.mvi

import net.asere.omni.core.Container

/**
 * A container that manages [State] and [Effect] for an MVI (Model-View-Intent) architecture.
 *
 * This is the base interface for all state containers in Omni MVI. It extends [Container],
 * inheriting [kotlinx.coroutines.CoroutineScope] and [kotlinx.coroutines.CoroutineExceptionHandler].
 *
 * @param State The type representing the UI state.
 * @param Effect The type representing side effects (one-off events).
 */
interface StateContainer<State : Any, Effect : Any> : Container
