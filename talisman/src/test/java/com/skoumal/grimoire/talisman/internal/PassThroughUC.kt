package com.skoumal.grimoire.talisman.internal

import com.skoumal.grimoire.talisman.UseCase
import kotlinx.coroutines.delay
import kotlin.random.Random.Default.nextLong

class PassThroughUC<In> : UseCase<In, In> {
    override suspend fun use(input: In): In {
        return input.also {
            delay(nextLong(100, 1000))
        }
    }
}