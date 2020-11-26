package com.skoumal.grimoire.talisman.seal

/**
 * Similar in functionality to [runCatching], but with key difference. It is detached from the
 * current kotlin version, hence should work regardless of version conflicts in your project.
 *
 * Failure is considered only as throwing an exception. Catching and returning exception is a
 * success.
 *
 * @return [Seal] which might be a failure or a success
 * */
inline fun <T, R : Any?> T.runSealed(body: T.() -> R): Seal<R> = letSealed(body)

/** @see [runSealed] */
inline fun <T, R : Any?> T.letSealed(body: (T) -> R): Seal<R> = try {
    Seal.success(body(this))
} catch (e: Throwable) {
    Seal.failure(e)
}


/**
 * Returns value stored in [Seal] only if it's a success, otherwise throws the exception stored
 * within according to [throwableOrThrow].
 * */
inline fun <reified T> Seal<T>.getOrThrow(): T {
    return when {
        isSuccess -> getOrNull() as T
        else -> throw throwableOrThrow()
    }
}

/**
 * Returns current throwable stored within [Seal]. If current [Seal] is a success then
 * [IllegalStateException] is thrown as requesting throwable at that state is illegal.
 * */
fun <T> Seal<T>.throwableOrThrow(): Throwable {
    return when {
        isFailure -> requireNotNull(throwableOrNull())
        else -> throw IllegalStateException("$this does not contain failure")
    }
}


/**
 * Updates the internally stored [Seal] value to output of this mapping function. Exceptions are
 * not caught, therefore propagated upstream. If you need to catch exceptions consider replacing
 * [map] with [mapSealed].
 *
 * In a case this [Seal] is not a success, the seal is cast to the new output [O] and therefore
 * this operation is considered free.
 * */
inline fun <reified I, O> Seal<I>.map(body: (I) -> O): Seal<O> {
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> Seal.success(body(getOrThrow()))
        else -> this as Seal<O>
    }
}

/**
 * In functionality identical to [map], however catches any exceptions thrown during the mapping
 * process
 *
 * @see map
 * */
inline fun <reified I, O> Seal<I>.mapSealed(body: (I) -> O): Seal<O> {
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> runSealed { body(getOrThrow()) }
        else -> this as Seal<O>
    }
}


/**
 * Offers internally stored value of [Seal] as input to [body] of this function and returns its
 * output. Otherwise on error casts this instance to the output class, this operation is free.
 *
 * Exceptions thrown while performing the [body] are not caught and propagate upstream immediately.
 * */
inline fun <reified I, O> Seal<I>.flatMap(body: (I) -> Seal<O>): Seal<O> {
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> body(getOrThrow())
        else -> this as Seal<O>
    }
}

/**
 * In functionality similar to [flatMap] however catches any exceptions that occur when executing
 * [body].
 * */
inline fun <reified I, reified O> Seal<I>.flatMapSealed(body: (I) -> Seal<O>): Seal<O> {
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> runSealed { flatMap(body).getOrThrow() }
        else -> this as Seal<O>
    }
}


/**
 * Loops through iterable internally stored, if present, and maps each value to output of [body].
 * Otherwise on error casts this instance to the output class, this operation is free.
 *
 * Exceptions thrown while performing the [body] are not caught and propagate upstream immediately.
 * */
inline fun <I, O> Seal<Iterable<I>>.listMap(body: (I) -> O): Seal<Iterable<O>> {
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> Seal.success(getOrThrow().map(body))
        else -> this as Seal<Iterable<O>>
    }
}

/**
 * In functionality similar to [listMap] however catches any exceptions that occur when executing
 * [body]. If exception occurs during mapping **any** object in the [Iterable] the whole [Seal]
 * is considered a failure and mapping will not be completed.
 * */
inline fun <I, O> Seal<Iterable<I>>.listMapSealed(body: (I) -> O): Seal<Iterable<O>> {
    return flatMapSealed { listMap(body) }
}


/**
 * Offers convenience functions to help with fetching the final value from [Seal]. Only one of
 * provided functions will be called at runtime depending on whether the [Seal] is a success or
 * not.
 * */
inline fun <reified I, O> Seal<I>.fold(
    onSuccess: (I) -> O,
    onFailure: (Throwable) -> O
): O {
    return when {
        isSuccess -> onSuccess(getOrThrow())
        else -> onFailure(throwableOrThrow())
    }
}


/**
 * Shorthand for keeping value in [Seal] the same but having default value when things go wrong.
 * */
inline fun <reified I> Seal<I>.onFailureReturn(body: (Throwable) -> I): I = fold(
    onSuccess = { it },
    onFailure = body
)

/**
 * Allows for inline simple returns without having throwable as a deciding factor.
 *
 * @see [onFailureReturn]
 * */
inline fun <reified I> Seal<I>.onFailureReturn(default: I): I = onFailureReturn { default }


/**
 * Invokes [body] only and only if the [Seal] contains a value - is a success
 * */
inline fun <reified I> Seal<I>.onSuccess(body: (I) -> Unit): Seal<I> = apply {
    when {
        isSuccess -> body(getOrThrow())
    }
}

/**
 * Invokes [body] only and only if the [Seal] contains an error - is a failure
 * */
inline fun <I> Seal<I>.onFailure(body: (Throwable) -> Unit): Seal<I> = apply {
    when {
        isFailure -> body(throwableOrThrow())
    }
}
