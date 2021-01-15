package com.skoumal.grimoire.talisman

import kotlin.random.Random.Default.nextInt

class UnitInputUC(
    private val body: suspend () -> Int = { nextInt() }
) : UseCase<Unit, Int> {

    override suspend fun use(input: Unit): Int {
        return body.invoke()
    }

}