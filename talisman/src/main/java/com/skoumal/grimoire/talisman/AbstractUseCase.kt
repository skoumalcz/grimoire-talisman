package com.skoumal.grimoire.talisman

import android.util.Log
import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
        return backing.transform(input).onFailureLog().onSuccess {
            if (!channel.sail(it)) {
                logInternal(
                    this@AbstractUseCase::class.java.simpleName,
                    "[ERROR] - internal use-case's channel cannot accept any values"
                )
            }
        }
    }

    private val channel = Vessel<Out>(Channel.CONFLATED)

    /**
     * By default, observe outputs an internal channel with up to one previous value. Channel is
     * automatically updated with new values as [invoke] is called.
     *
     * Causing channel failure is your responsibility and doing so will inevitably corrupt the
     * feature and may cause crashes. Channel does not close automatically and therefore is opened
     * for the duration of this class' lifespan.
     *
     * You can override this method and replace the functionality with Flows from Room (database)
     * or entirely different approach - such as repeated polling of an endpoint and such.
     * */
    override fun observe(input: In): Flow<Out> {
        return channel.dock()
    }

    /**
     * On any failure that [backing] or [run] has produced runs [Seal.onFailure] method to check
     * whether it can log the failure. If so, then the throwable is logged via [println][println].
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

        logInternal(this@AbstractUseCase::class.java.simpleName, it)
    }

    private fun logInternal(tag: String, throwable: Throwable) =
        logInternal(tag, throwable.stackTraceToString())

    private fun logInternal(tag: String, message: String) {
        val printable = StringBuilder(tag)
            .appendLine()
            .append(message)
            .toString()

        println(printable)
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