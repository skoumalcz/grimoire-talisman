package com.skoumal.grimoire.talisman

import kotlinx.coroutines.flow.Flow

interface UseCaseFlow<In : Any?, Out : Any?> {

    fun observe(input: In): Flow<Out>

}