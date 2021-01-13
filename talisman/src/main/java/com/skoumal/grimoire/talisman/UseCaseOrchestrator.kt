package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Orchestrators are used as a mediator between the consumer and the use case. They are beneficial
 * because they provide additional functionality on top of the "basic" use cases.
 *
 * The functionality can be extended by the consumers such as you by making an extension on top of
 * the [companion][UseCaseOrchestrator] object.
 *
 * It is strongly suggested that, if it comes to performance intensive tasks, you should NOT use
 * the provided dynamic extensions and operators, but rather declare a static class to reduce
 * overhead.
 *
 * @see fast
 * @see simple
 * @see switching
 * @see racing
 * */
interface UseCaseOrchestrator<I, O> {

    /**
     * Runs the transformation and catches all exceptions thrown while executing the code. If the
     * declaration fails to meet this requirement it's considered as breaking of compatibility and
     * common contract with the consumer.
     * */
    suspend fun run(input: I): Seal<O>

    companion object

}

/**
 * Creates new orchestrator as a simple anonymous class. Every exception thrown while executing
 * contents of [run] must be caught, unconditionally.
 *
 * This is best done with wrapping the contents with [runSealed].
 * */
inline fun <In, Out> UseCaseOrchestrator(
    crossinline run: suspend (input: In) -> Seal<Out>
) = object : UseCaseOrchestrator<In, Out> {
    override suspend fun run(input: In): Seal<Out> = run.invoke(input)
}

/**
 * Tries to decide which orchestrator is the best for given situation. If and only if the [context]
 * provided by the consumer is [Dispatchers.Unconfined] then no switching is performed and [simple]
 * orchestrator is returned. In the other case [switching] is the orchestrator of choice.
 * */
fun <In, Out> UseCaseOrchestrator.Companion.fast(
    context: CoroutineContext,
    useCase: UseCase<In, Out>
) = when (context) {
    Dispatchers.Unconfined -> simple(useCase)
    else -> switching(context, useCase)
}

/**
 * Create a new instance of simple orchestrator. It will simply start executing the use case and
 * wrapping the result with [Seal]. Nothing more, nothing less.
 * */
fun <In, Out> UseCaseOrchestrator.Companion.simple(
    useCase: UseCase<In, Out>
) = UseCaseOrchestrator<In, Out> { runSealed { useCase.use(it) } }

/**
 * Create a new instance of switching orchestrator. It will unconditionally switch to the provided
 * context as per [withContext]. After switching to that context it will immediately start executing
 * the use case and wrapping the result with [Seal].
 * */
fun <In, Out> UseCaseOrchestrator.Companion.switching(
    context: CoroutineContext = Dispatchers.Default,
    useCase: UseCase<In, Out>
) = UseCaseOrchestrator<In, Out> {
    withContext(context) { runSealed { useCase.use(it) } }
}

/**
 * Creates a new instance of racing orchestrator. The behavior is very much similar to [fast],
 * however launches all input tasks on given [useCase] in parallel. It waits for all the jobs to
 * complete and returns first failure or success.
 *
 * As mentioned, it internally selects best implementation of underlying orchestrator based on
 * [fast]'s implementation and uses that single instance to wrap all subsequent jobs.
 *
 * Jobs for each input given will be launched with a provided context, which defaults to
 * [Dispatchers.Default]. If the selected dispatcher is [Dispatchers.Unconfined] then context
 * switching will be bypassed and execution will be less expensive.
 * */
fun <In> UseCaseOrchestrator.Companion.racing(
    context: CoroutineContext = Dispatchers.Default,
    useCase: UseCase<In, Unit>
) = UseCaseOrchestrator<Iterable<In>, Unit> { inputs ->
    val orchestrator = fast(context, useCase)
    val results = inputs.map { input ->
        coroutineScope {
            async(context = context) {
                orchestrator.run(input)
            }
        }
    }

    results.awaitAll().firstOrNull { it.isFailure } ?: Seal.success(Unit)
}

/** @see run */
suspend operator fun <In, Out> UseCaseOrchestrator<In, Out>.invoke(input: In): Seal<Out> =
    run(input)