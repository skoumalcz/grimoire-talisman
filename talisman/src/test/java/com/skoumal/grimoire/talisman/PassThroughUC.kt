package com.skoumal.grimoire.talisman

import kotlinx.coroutines.delay
import kotlin.random.Random.Default.nextLong

class PassThroughUC<In> : UseCase<In, In> {
    override suspend fun use(input: In): In {
        return input.also {
            delay(nextLong(100, 1000))
        }
    }
}