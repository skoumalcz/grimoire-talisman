package com.skoumal.grimoire.talisman

import android.util.Log
import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class AbstractUseCase<In, Out> : UseCase<In, Out> {

    /**
     * Logging errors can be enabled/disabled by settings this property to true/false respectively.
     * By default [logErrors] is enabled and dumps all failures to [Warning][Log.w] channel.
     * */
    protected open val logErrors: Boolean = true

    /**
     * Backing is in charge of transforming inputs to [sealed][Seal] outputs via [UseCaseBacking].
     * Consumers are free to reimplement this API to suite their needs. Default value is
     * [ContextSwitchingBacking] with [Default][Dispatchers.Default] context.
     * */
    protected open val backing: UseCaseBacking<In, Out> = ContextSwitchingBacking()

    /**
     * Allows you to implement the use-case with custom logic, transforming [input][In] to
     * [output][Out]. Method might throw an exception and is called by
     * [backing implementation][backing] after starting the execution through [invoke].
     *
     * @throws Throwable function is permitted to throw exceptions, [backing] is in charge of
     * catching them
     *
     * @see [backing]
     * */
    protected abstract suspend fun run(input: In): Out

    /**
     * Invoke emulates entry point to the class. Provided input transforms to output with the help
     * of [backing].
     *
     * Backing always calls its method [transform][UseCaseBacking.transform] which has custom
     * implementation to allow transformation to [Seal]. Backing can be opinionated about the
     * content it's trying to transform and preemptively prevent it from reaching [run].
     *
     * Finally after the transformation is complete, it optionally logs all failures via
     * [onFailureLog].
     *
     * @see backing
     * @see UseCaseBacking.transform
     * @see onFailureLog
     * */
    final override suspend fun invoke(input: In): Seal<Out> {
        return backing.transform(input).onFailureLog()
    }

    /**
     * On any failure that [backing] or [run] has produced runs [Seal.onFailure] method to check
     * whether it can log the failure. If so, then the throwable is logged via [Warning][Log.w].
     *
     * This function can be reimplemented to pass throwable through Timber for an instance.
     * This cannot be recommended and you should refrain from doing so, otherwise you can
     * accidentally leak all exceptions to crashlytics or similar tools and dilute the real
     * problems.
     * */
    protected open fun Seal<Out>.onFailureLog() = onFailure {
        if (!logErrors) {
            return@onFailure
        }
        Log.w(this@AbstractUseCase::class.java.simpleName, it)
    }


    // region Backings

    inner class ContextSwitchingBacking(
        private val context: CoroutineContext = Dispatchers.Default
    ) : UseCaseBacking<In, Out> {
        override suspend fun transform(input: In): Seal<Out> {
            return withContext(context) {
                runSealed { run(input) }
            }
        }
    }

    inner class SimpleBacking : UseCaseBacking<In, Out> {
        override suspend fun transform(input: In): Seal<Out> {
            return runSealed { run(input) }
        }
    }

    // endregion

}