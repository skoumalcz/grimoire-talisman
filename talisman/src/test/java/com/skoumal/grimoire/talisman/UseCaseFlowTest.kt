@file:Suppress("ClassName")

package com.skoumal.grimoire.talisman

import com.google.common.truth.Truth.assertThat
import com.skoumal.grimoire.talisman.internal.FlowUC
import com.skoumal.grimoire.talisman.internal.FlowUnitUC
import com.skoumal.grimoire.talisman.internal.nextString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.random.Random.Default.nextInt

class UseCaseFlowTest {

    private lateinit var uc: FlowUC
    private lateinit var flow: Flow<String>

    @Before
    fun prepare() {
        uc = FlowUC()
        flow = flow {
            for (i in 0 until nextInt(1, 10)) {
                emit(nextString)
            }
        }
    }

    // ---

    @Test
    fun `observe provides unmodified values`() {
        val output = uc.observe(flow)
        assertThat(output).isSameInstanceAs(flow)
    }

    @Test
    fun `observe provides unconsumed values`() {
        val output = uc.observe(flow)
        runBlockingTest {
            val listInput = flow.toList()
            val listOutput = output.toList()

            assertThat(listInput).isNotEmpty()
            assertThat(listOutput).isNotEmpty()
        }
    }

}

class UseCaseFlowTest_UnitInput {

    private lateinit var uc: FlowUnitUC
    private lateinit var flow: Flow<String>

    @Before
    fun prepare() {
        flow = flow {
            for (i in 0 until nextInt(1, 10)) {
                emit(nextString)
            }
        }
        uc = FlowUnitUC(flow)
    }

    // ---

    @Test
    fun `delegated value provides unmodified values`() {
        val output by uc
        assertThat(output).isSameInstanceAs(flow)
    }

    @Test
    fun `observe provides unconsumed values`() {
        val output by uc
        runBlockingTest {
            val listInput = flow.toList()
            val listOutput = output.toList()

            assertThat(listInput).isNotEmpty()
            assertThat(listOutput).isNotEmpty()
        }
    }

}
