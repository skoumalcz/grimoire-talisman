package com.skoumal.grimoire.talisman

import com.skoumal.grimoire.talisman.seal.*

interface UseCase<In : Any?, Out : Any?> {

    suspend operator fun invoke(input: In): Seal<Out>

}