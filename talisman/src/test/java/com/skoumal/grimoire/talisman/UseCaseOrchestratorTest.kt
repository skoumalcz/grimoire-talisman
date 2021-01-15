package com.skoumal.grimoire.talisman

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class UseCaseOrchestratorTest {

    // --- switching

    @Test
    fun `switching doesn't switch when unconfined`() {
        val context = Dispatchers.Default
        val uc = UnitOutputUC {
            assertThat(context in coroutineContext).isTrue()
            nextInt()
        }

        runBlocking(context) {
            UseCaseOrchestrator
                .switching(uc, Dispatchers.Unconfined)
                .invoke(0)
        }
    }

    @Test
    fun `switching doesn't switch when identical`() {
        val context = Dispatchers.Default
        val uc = UnitOutputUC {
            assertThat(context in coroutineContext).isTrue()
            nextInt()
        }

        runBlocking(context) {
            UseCaseOrchestrator
                .switching(uc, Dispatchers.Default)
                .invoke(0)
        }
    }

    @Test
    fun `switching switches when other than current`() {
        val context = Dispatchers.Default
        val starterContext = Dispatchers.IO
        val uc = UnitOutputUC {
            assertThat(context in coroutineContext).isTrue()
            assertThat(starterContext in coroutineContext).isFalse()
        }

        runBlocking(starterContext) {
            UseCaseOrchestrator
                .switching(uc, context)
                .invoke(0)
        }
    }

    // --- simple

    @Test
    fun `simple doesn't switch with unconfined`() {
        `check simple doesn't switch`(
            context = Dispatchers.Unconfined,
            starterContext = Dispatchers.Default
        )
    }

    @Test
    fun `simple doesn't switch with default`() {
        `check simple doesn't switch`(
            context = Dispatchers.Default,
            starterContext = Dispatchers.IO
        )
    }

    @Test
    fun `simple doesn't switch with io`() {
        `check simple doesn't switch`(
            context = Dispatchers.IO,
            starterContext = Dispatchers.Default
        )
    }

    @Test
    fun `simple doesn't switch with main`() {
        `check simple doesn't switch`(
            context = Dispatchers.Main,
            starterContext = Dispatchers.Default
        )
    }

    private fun `check simple doesn't switch`(
        context: CoroutineContext,
        starterContext: CoroutineContext
    ) {
        val uc = UnitOutputUC {
            assertThat(context in coroutineContext).isFalse()
            assertThat(starterContext in coroutineContext).isTrue()
        }

        runBlocking(starterContext) {
            UseCaseOrchestrator
                .simple(uc)
                .invoke(0)
        }
    }

    // --- racing

    @Test
    fun `racing returns error when at least one fails`() {
        val uc = UnitOutputUC {
            if (it == 3) {
                throw IllegalStateException("This is designed to fail")
            }
            delay(nextLong(100, 1000))
        }
        val numbers = listOf(1, 2, 3, 4, 5)
        val context = TestCoroutineDispatcher()

        runBlockingTest(context) {
            val result = UseCaseOrchestrator
                .racing(uc, context, this)
                .invoke(numbers)

            assertThat(result.isFailure).isTrue()
            assertThat(result.throwableOrNull()).isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Test
    fun `racing returns values when none fails`() {
        val uc = PassThroughUC<Int>()
        val numbers = listOf(1, 2, 3, 4, 5)
        val context = TestCoroutineDispatcher()

        runBlockingTest(context) {
            val result = UseCaseOrchestrator
                .racing(uc, context, this)
                .invoke(numbers)

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).containsExactlyElementsIn(numbers)
        }
    }

}