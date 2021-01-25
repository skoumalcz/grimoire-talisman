package com.skoumal.grimoire.talisman

import kotlin.reflect.KProperty

/**
 * Simple interface allowing synchronous code to be executed in a similar fashion as the
 * asynchronous one (using [UseCase]).
 *
 * It can be used directly, however more concise approach is to use the [with] operator.
 * [UseCaseSimple] will be wrapped in an object that optimizes the getter delivery and allows
 * delegation.
 *
 * ## Example:
 * ```
 * class MyViewModel(private val uc: SimpleUseCase<Int, Int>) {
 *
 *     var delegatedProperty by uc withInput 0
 *
 * }
 * ```
 *
 * If you do not wish to use delegation, make sure to not use the [with] operator as it introduces
 * unnecessary overhead.
 * */
interface UseCaseSimple<In : Any?, Out : Any?> {

    /**
     * Main execution point of use case's logic.
     *
     * It is not recommended to do a long running jobs within use case of _this_ type. If you plan
     * to do a long running jobs, use [UseCase] instead.
     *
     * It is also permitted to use this method directly as you are **NOT permitted to throw
     * exceptions** within the body of [use] method. It would be confusing to allow delegation and
     * having extensions to deal with throwing exception - this would lead in having the use cases
     * misused and potentially causing unwanted crashes.
     * */
    fun use(input: In): Out

}

/**
 * Class responsible for keeping last input given to execute a use case.
 * */
class KeepingValue<In, Out> internal constructor(
    @JvmField internal val useCase: UseCaseSimple<In, Out>,
    initial: In
) {

    @Volatile
    @JvmField
    internal var lastInput = initial

}

/**
 * Creates a [KeepingValue] to allow delegation. Every time you read a value it invokes a
 * [UseCase.use] with a last input.
 *
 * Until a [setValue] is called, then [initialInput] is passed as an input. Otherwise the next time
 * [setValue] is called, its value is stored within [KeepingValue] and used thereafter.
 * */
infix fun <In, Out> UseCaseSimple<In, Out>.with(initialInput: In) =
    KeepingValue(useCase = this, initial = initialInput)

/** @see with */
operator fun <In, Out> KeepingValue<In, Out>.getValue(
    out: Out?,
    prop: KProperty<*>
): Out = useCase.use(lastInput)

/** @see with */
operator fun <In, Out> KeepingValue<In, Out>.setValue(
    out: Out?,
    prop: KProperty<*>,
    value: In
) {
    useCase.use(value)
    lastInput = value
}