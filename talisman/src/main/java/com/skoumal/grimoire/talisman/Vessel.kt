package com.skoumal.grimoire.talisman

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * You are also required to manage the "sinking" of your vessel. After you're done with sending data
 * through this vessel you can safely call [sink], the channel will be marked as closed for sending
 * and is automatically disposed of once all elements are received. See [Channel.close] for
 * reference.
 * */
@OptIn(ExperimentalCoroutinesApi::class)
class Vessel<Sailor>(private val size: Int = Channel.RENDEZVOUS) {

    @Volatile
    private lateinit var channel: Channel<Sailor>

    private fun getChannel(
        requireImplicitlyNew: Boolean = ::channel.isInitialized && channel.isClosedForSend
    ): Channel<Sailor> {
        if (!::channel.isInitialized || requireImplicitlyNew) {
            return Channel<Sailor>(size).also { channel = it }
        }
        return channel
    }

    /**
     * Offer a sailor to the sea (of the current channel, or creates a new channel if it was
     * closed before) and returns the result. Sailor can be rejected if channel exceeds its size.
     *  */
    fun sail(sailor: Sailor) = getChannel().offer(sailor)

    /**
     * Returns current _stream_ to which new sailors can be sent. Optionally you can request
     * entirely new stream rendering the old stream closed and unusable.
     * */
    fun dock(openNew: Boolean = false) = getChannel(openNew).consumeAsFlow()

    /**
     * Sinks docked vessel making all provided flows through [dock] completed.
     *
     * @return whether channel has been closed or not. Channel can be uninitialized which returns
     * false, since there's nothing to close
     * */
    fun sink() = if (::channel.isInitialized) channel.close() else false

}