package com.skoumal.grimoire.talisman.internal

import com.skoumal.grimoire.talisman.UseCase
import kotlin.random.Random.Default.nextInt

class UnitInputUC(
    private val body: suspend () -> Int = { nextInt() }
) : UseCase<Unit, Int> {

    override suspend fun use(input: Unit): Int {
        return body.invoke()
    }

}