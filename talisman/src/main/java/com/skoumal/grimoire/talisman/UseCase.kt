package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

interface UseCase<In : Any?, Out : Any?> {

    /**
     * Main execution point of use case's logic.
     *
     * It's permitted to launch long running jobs and throw exception(s).
     *
     * It's absolutely forbidden to call this method directly as it needs a mediator such as
     * [UseCaseOrchestrator]. If you wish to _not_ use such mediator you're required to declare
     * that you're returning a [Seal], catch all thrown exceptions and switch contexts manually.
     *
     * Launch all jobs using [invoke] if possible.
     * @see invoke
     * */
    @Throws
    suspend fun use(input: In): Out

}

/**
 * Launches a fast orchestrator on top of the provided data.
 * @see switching
 * */
suspend operator fun <In, Out> UseCase<In, Out>.invoke(
    input: In,
    context: CoroutineContext = Dispatchers.Default
): Seal<Out> = UseCaseOrchestrator
    .switching(this, context)
    .invoke(input)

/**
 * Convenience method to forbid [Unit] as an input parameter.
 * @see invoke
 * */
suspend operator fun <Out> UseCase<Unit, Out>.invoke(
    context: CoroutineContext = Dispatchers.Default
): Seal<Out> = UseCaseOrchestrator
    .switching(this, context)
    .invoke(Unit)

/**
 * Launches a racing orchestrator on top of the provided data.
 * @see racing
 * */
suspend operator fun <In, Out> UseCase<In, Out>.invoke(
    input: Iterable<In>,
    context: CoroutineContext = Dispatchers.Default
): Seal<Iterable<Out>> = coroutineScope {
    UseCaseOrchestrator
        .racing(useCase = this@invoke, context = context, scope = this)
        .invoke(input)
}