package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*

interface UseCaseBacking<I, O> {

    suspend fun transform(input: I): Seal<O>

}