package com.skoumal.grimoire.talisman.seal

inline class Seal<out T : Any?>(
    private val value: Any?
) {

    val isSuccess get() = value !is Error
    val isFailure get() = value is Error

    @Suppress("UNCHECKED_CAST")
    fun getOrNull() = when {
        isSuccess -> value as T
        else -> null
    }

    fun throwableOrNull() = when {
        isFailure -> (value as Error).throwable
        else -> null
    }

    // ---

    internal class Error(
        val throwable: Throwable
    ) {
        override fun toString() = "Seal.Error[throwable=$throwable]"
    }

    // ---

    companion object {

        @JvmStatic
        fun <T : Any?> success(result: T) = Seal<T>(result)

        @JvmStatic
        fun <T : Any?> failure(throwable: Throwable) = Seal<T>(Error(throwable))

    }

}