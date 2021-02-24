package com.skoumal.grimoire.talisman.internal

import com.skoumal.grimoire.talisman.UseCase

class UnitOutputUC(
    private val body: suspend (Int) -> Unit = {}
) : UseCase<Int, Unit> {

    override suspend fun use(input: Int) {
        body.invoke(input)
    }

}