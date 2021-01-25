package com.skoumal.grimoire.talisman

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random.Default.nextInt

class UseCaseSimpleTest {

    @Test
    fun `writing to keeping value updates input`() {
        val initial = nextInt()
        val uc = TestUC() with initial
        var delegated by uc

        val nextInput = nextInt()
        delegated = nextInput

        assertThat(uc.lastInput).isEqualTo(nextInput)
        assertThat(delegated).isEqualTo(nextInput)
    }

    @Test
    fun `initialInput is lastInput`() {
        val initial = nextInt()
        val uc = TestUC().with(initial)
        val delegated by uc

        assertThat(delegated).isEqualTo(initial)
        assertThat(uc.lastInput).isEqualTo(initial)
    }

    @Test
    fun `use is called every time getter is called`() {
        var called = false
        val uc = TestUC {
            called = true
            it
        } with 0
        val delegated by uc

        assertThat(called).isFalse()
        assertThat(delegated).isEqualTo(0) // just a getter invocation
        assertThat(called).isTrue()

        called = false

        assertThat(delegated).isEqualTo(0) // just a getter invocation
        assertThat(called).isTrue()
    }

    private class TestUC(
        private val use: (Int) -> Int = { it }
    ) : UseCaseSimple<Int, Int> {
        override fun use(input: Int): Int {
            return use.invoke(input)
        }
    }

}