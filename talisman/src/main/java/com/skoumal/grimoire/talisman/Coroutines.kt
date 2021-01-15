package com.skoumal.grimoire.talisman

import kotlin.coroutines.CoroutineContext

internal operator fun CoroutineContext.contains(
    context: CoroutineContext
): Boolean = fold(this === context) { contains, inner ->
    contains || inner === context
}