package com.madapp.omni.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

@OptIn(ExperimentalCoroutinesApi::class)
fun Channel<*>.isClosed() = this.isClosedForReceive && this.isClosedForSend