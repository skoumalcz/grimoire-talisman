package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*
import kotlinx.coroutines.flow.Flow

interface UseCase<In : Any?, Out : Any?> {

    suspend operator fun invoke(input: In): Seal<Out>

    fun observe(input: In): Flow<Out>

}