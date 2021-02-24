package com.skoumal.grimoire.talisman.internal

import com.skoumal.grimoire.talisman.UseCaseFlow
import kotlinx.coroutines.flow.Flow

class FlowUC : UseCaseFlow<Flow<String>, String> {

    override fun flow(input: Flow<String>): Flow<String> {
        return input
    }

}

class FlowUnitUC(private val input: Flow<String>) : UseCaseFlow<Unit, String> {

    override fun flow(input: Unit): Flow<String> {
        return this.input
    }

}