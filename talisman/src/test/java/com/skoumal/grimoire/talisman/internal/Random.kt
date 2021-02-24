package com.skoumal.grimoire.talisman.internal

import kotlin.random.Random.Default.nextBytes
import kotlin.random.Random.Default.nextInt

val nextString
    get() = String(nextBytes(nextInt(1, 300)))