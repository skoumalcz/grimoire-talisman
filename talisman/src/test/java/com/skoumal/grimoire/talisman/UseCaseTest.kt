package com.skoumal.grimoire.talisman

import com.google.common.truth.Truth.assertThat
import com.skoumal.grimoire.talisman.internal.UnitInputUC
import com.skoumal.grimoire.talisman.internal.UnitOutputUC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.coroutineContext
import kotlin.random.Random.Default.nextInt

@Suppress("EXPERIMENTAL_API_USAGE")
class UseCaseTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    // ---

    @Test
    fun `use case shouldn't override context`() {
        val context = Dispatchers.Main
        val uc = UnitInputUC {
            // if doesn't contain this context then it's been switched
            assertThat(context in coroutineContext).isTrue()
            nextInt()
        }

        runBlocking(context) { uc.use(Unit) }
    }

    @Test
    fun `use case should return predefined value`() {
        val value = nextInt()
        val context = Dispatchers.Default
        val uc = UnitInputUC { value }

        runBlocking(context) { assertThat(uc.use(Unit)).isEqualTo(value) }
    }

    // ---

    @Test
    fun `use case invoke method returns predefined value`() {
        val value = nextInt()
        val context = Dispatchers.Main
        val uc = UnitInputUC {
            assertThat(context in coroutineContext).isTrue()
            value
        }

        runBlocking(context) {
            assertThat(uc(Dispatchers.Unconfined).getOrNull()).isEqualTo(value)
        }
    }

    @Test
    fun `use case invoke method with input returns predefined value`() {
        val value = nextInt()
        val context = Dispatchers.Main
        val uc = UnitInputUC {
            assertThat(context in coroutineContext).isTrue()
            value
        }

        runBlocking(context) {
            assertThat(uc(Unit, Dispatchers.Unconfined).getOrNull()).isEqualTo(value)
        }
    }

    // ---

    @Test
    fun `invoke with input uses Dispatchers-Default as default`() {
        val context = Dispatchers.Default
        val uc = UnitInputUC {
            assertThat(context in coroutineContext).isTrue()
            nextInt()
        }

        runBlocking { uc(Unit) }
    }

    @Test
    fun `invoke without input uses Dispatchers-Default as default`() {
        val context = Dispatchers.Default
        val uc = UnitInputUC {
            assertThat(context in coroutineContext).isTrue()
            nextInt()
        }

        runBlocking { uc() }
    }

    @Test
    fun `invoke with list input uses Dispatchers-Default as default`() {
        val context = Dispatchers.Default
        val uc = UnitOutputUC {
            assertThat(context in coroutineContext).isTrue()
        }

        runBlocking { uc(listOf(1, 2, 3)) }
    }

}