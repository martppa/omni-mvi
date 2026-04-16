package net.asere.omni.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

/**
 * Checks if the [Channel] is fully closed.
 *
 * A channel is considered fully closed when it is closed for both sending and receiving.
 * This is useful for determining if the channel needs to be recreated or restarted.
 *
 * @return `true` if the channel is closed for both send and receive operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Channel<*>.isClosed() = this.isClosedForReceive && this.isClosedForSend
