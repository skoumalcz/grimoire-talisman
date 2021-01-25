package com.skoumal.grimoire.talisman

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty

interface UseCaseFlow<In : Any?, Out : Any?> {

    /**
     * Allows for a use case to provide flow with values. This is typically for cases where
     * observing data from database is necessary.
     *
     * You should never call this method directly, instead use [observe] for more convenience.
     * */
    fun flow(input: In): Flow<Out>

}

/**
 * Convenience function to rationally call observe on unknown generic flow.
 * @see UseCaseFlow.flow
 * */
fun <In, Out> UseCaseFlow<In, Out>.observe(input: In) = flow(input)

/**
 * Convenience function to rationally call observe on flow with [Unit] input type.
 * @see UseCaseFlow.flow
 * */
fun <Out> UseCaseFlow<Unit, Out>.observe() = flow

/**
 * Convenience function to rationally call observe on flow with [Unit] input type.
 * @see UseCaseFlow.flow
 * */
val <Out> UseCaseFlow<Unit, Out>.flow
    get() = observe(Unit)

/**
 * Provides a delegated value to the receiver.
 * */
operator fun <Out> UseCaseFlow<Unit, Out>.getValue(any: Any, prop: KProperty<*>): Flow<Out> {
    return observe()
}