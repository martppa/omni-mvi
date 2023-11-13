package net.asere.omni.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

/**
 * Tells you whether the channel is totally closed. Totally closed
 * stands for closed for receive and for send.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Channel<*>.isClosed() = this.isClosedForReceive && this.isClosedForSend