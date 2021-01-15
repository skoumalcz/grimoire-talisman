package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

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
 * Create a new instance of simple orchestrator. It will simply start executing the use case and
 * wrapping the result with [Seal]. Nothing more, nothing less.
 * */
fun <In, Out> UseCaseOrchestrator.Companion.simple(
    useCase: UseCase<In, Out>
): UseCaseOrchestrator<In, Out> = SimpleOrchestrator(useCase)

private class SimpleOrchestrator<In, Out>(
    private val useCase: UseCase<In, Out>
) : UseCaseOrchestrator<In, Out> {
    override suspend fun run(input: In): Seal<Out> {
        return runSealed { useCase.use(input) }
    }
}

/**
 * Create a new instance of switching orchestrator. It tries to decide which orchestrator is the
 * best for given situation.
 *
 * It will switch context whenever it's:
 *
 * 1) not exactly same as the current context
 * 2) does not contain the exact same context
 * 3) is not instance of [Dispatchers.Unconfined]
 *
 * In those cases the behavior is identical to [simple]. In the remaining cases it will let
 * [withContext] decide whether it wants to switch contexts and executes the [useCase] while being
 * wrapped in context according to [withContext].
 *
 * All results are wrapped and exceptions caught with [Seal].
 *
 * @see [simple]
 * @see [withContext]
 * */
fun <In, Out> UseCaseOrchestrator.Companion.switching(
    useCase: UseCase<In, Out>,
    context: CoroutineContext
): UseCaseOrchestrator<In, Out> = SwitchingOrchestrator(useCase, context)

private class SwitchingOrchestrator<In, Out>(
    private val useCase: UseCase<In, Out>,
    private val context: CoroutineContext
) : UseCaseOrchestrator<In, Out> {
    override suspend fun run(input: In): Seal<Out> {
        // whenever we are already on that context, we will not bother with switching contexts,
        // that is just unnecessary overhead with which we don't need to prolong the execution of
        // the (simple) use case
        if (
            context in coroutineContext ||
            Dispatchers.Unconfined in context
        ) {
            return runInternal(input)
        }

        // otherwise just roll with it
        return withContext(context) {
            runInternal(input)
        }
    }

    private suspend fun runInternal(input: In): Seal<Out> =
        runSealed { useCase.use(input) }
}

/**
 * Creates a new instance of racing orchestrator. The behavior is very much similar to [switching],
 * however launches all input tasks on given [useCase] in parallel. It waits for all the jobs to
 * complete and returns first failure or success.
 *
 * As mentioned, it internally selects best implementation of underlying orchestrator based on
 * [switching]'s implementation and uses that single instance to wrap all subsequent jobs.
 *
 * Jobs for each input given will be launched with a provided context, which defaults to
 * [Dispatchers.Default]. If the selected dispatcher is [Dispatchers.Unconfined] then context
 * switching will be bypassed and execution will be less expensive.
 * */
fun <In, Out> UseCaseOrchestrator.Companion.racing(
    useCase: UseCase<In, Out>,
    context: CoroutineContext,
    scope: CoroutineScope
): UseCaseOrchestrator<Iterable<In>, List<Out>> = RacingOrchestrator(useCase, context, scope)

private class RacingOrchestrator<In, Out>(
    useCase: UseCase<In, Out>,
    context: CoroutineContext,
    private val scope: CoroutineScope
) : UseCaseOrchestrator<Iterable<In>, List<Out>> {

    private val orchestrator = UseCaseOrchestrator.switching(useCase, context)

    override suspend fun run(input: Iterable<In>): Seal<List<Out>> {
        val jobs = input.map { single ->
            scope.async {
                orchestrator(single)
            }
        }

        return jobs.awaitAll().flatten()
    }
}

/**
 * Convenience method for [run]
 * @see UseCaseOrchestrator.run
 * */
suspend operator fun <In, Out> UseCaseOrchestrator<In, Out>.invoke(input: In): Seal<Out> =
    run(input)