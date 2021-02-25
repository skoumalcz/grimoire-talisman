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
     * exceptions** within the body of [getValue] method. It would be confusing to allow delegation and
     * having extensions to deal with throwing exception - this would lead in having the use cases
     * misused and potentially causing unwanted crashes.
     * */
    fun getValue(): Out

    /**
     * Allows for a setting value to this particular use case. It is not mandatory and by default
     * has no-op implementation.
     *
     * It is not recommended to do a long running jobs within use case of _this_ type. If you plan
     * to do a long running jobs, use [UseCase] instead.
     * */
    fun setValue(input: In) {}

}

/** @see with */
operator fun <In, Out> UseCaseSimple<In, Out>.getValue(
    parent: Any?,
    prop: KProperty<*>
): Out = getValue()

/** @see with */
operator fun <In, Out> UseCaseSimple<In, Out>.setValue(
    parent: Any?,
    prop: KProperty<*>,
    value: In
) = setValue(value)