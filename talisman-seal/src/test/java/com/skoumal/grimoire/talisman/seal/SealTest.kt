package com.skoumal.grimoire.talisman.seal

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random.Default.nextBytes
import kotlin.random.Random.Default.nextInt

class SealTest {

    @Test
    fun `Seal returns null throwable when success`() {
        val seal = Seal.success(0)

        assertThat(seal.throwableOrNull()).isNull()
    }

    @Test
    fun `Seal returns null value when failure`() {
        val seal = Seal.failure<Int>(Throwable())

        assertThat(seal.getOrNull()).isNull()
    }

    @Test
    fun `runSealed catches exceptions`() {
        val exception = NotImplementedError()
        val result = runSealed { throw exception }.throwableOrThrow()

        assertThat(result).isSameInstanceAs(exception)
    }

    @Test
    fun `runSealed returns sealed value`() {
        val value = nextInt()
        val result = runSealed { value }

        assertThat(result.getOrNull()).isEqualTo(value)
    }

    @Test
    fun `letSealed catches exceptions`() {
        val exception = NotImplementedError()
        val result = letSealed { throw exception }.throwableOrThrow()

        assertThat(result).isSameInstanceAs(exception)
    }

    @Test
    fun `letSealed returns sealed value`() {
        val value = nextInt()
        val result = letSealed { value }

        assertThat(result.getOrNull()).isEqualTo(value)
    }

    @Test
    fun `getOrThrow returns value from success`() {
        val value = nextInt()
        val seal = Seal.success(value)

        assertThat(seal.getOrNull()).isEqualTo(value)
    }

    @Test
    fun `getOrThrow throws from failure`() {
        val seal = Seal.failure<Int>(NotImplementedError())

        try {
            seal.getOrThrow()
            assert(false)
        } catch (ok: NotImplementedError) {
        }
    }

    @Test
    fun `throwableOrThrow return throwable from failure`() {
        val throwable = NotImplementedError()
        val seal = Seal.failure<Int>(throwable)

        assertThat(seal.throwableOrNull()).isSameInstanceAs(throwable)
    }

    @Test
    fun `throwableOrThrow throws from success`() {
        val seal = Seal.success(nextInt())

        try {
            seal.throwableOrThrow()
            assert(false)
        } catch (ok: IllegalStateException) {
        }
    }

    @Test
    fun `map updates value when success`() {
        val initialValue = nextInt()
        val mappedValue = nextBytes(30)
        val seal = Seal.success(initialValue)

        val result = seal.map { mappedValue }
        assertThat(result.getOrThrow()).isSameInstanceAs(mappedValue)
    }

    @Test
    fun `map keeps throwable when failure`() {
        val mappedValue = nextBytes(30)
        val throwable = NotImplementedError()
        val seal = Seal.failure<Int>(throwable)
        val result = seal.map { mappedValue }

        try {
            result.getOrThrow()
            assert(false)
        } catch (ok: NotImplementedError) {
            assertThat(ok).isSameInstanceAs(throwable)
        }
    }

    @Test
    fun `mapSealed catches throwable when success`() {
        val seal = Seal.success(nextInt())
        val throwable = IllegalArgumentException()
        val result = seal.mapSealed<Int, Byte> { throw throwable }

        assertThat(seal).isNotSameInstanceAs(result)
        assertThat(result.throwableOrNull()).isSameInstanceAs(throwable)
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `mapSealed keeps throwable when failure`() {
        val throwable = IllegalArgumentException()
        val thrown = NotImplementedError()
        val seal = Seal.failure<Int>(throwable)
        val result = seal.mapSealed<Int, Byte> { throw thrown }

        // recycles the same instance for efficiency (starts as failure, is only cast to other result)
        assertThat(seal).isSameInstanceAs(result)
        assertThat(seal).isEqualTo(result)
        assertThat(seal.throwableOrNull()).isSameInstanceAs(throwable)
    }

    @Test
    fun `flatMap updates value when success`() {
        val mappedValue = Seal.success(nextBytes(30))
        val seal = Seal.success(nextInt())
        val result = seal.flatMap { mappedValue }

        // the unwrapping shouldn't be done at this point to improve efficiency
        assertThat(result).isSameInstanceAs(mappedValue)
        assertThat(result).isEqualTo(mappedValue)
    }

    @Test
    fun `flatMap keeps throwable when failure`() {
        val mappedValue = IllegalArgumentException()
        val seal = Seal.failure<Int>(NotImplementedError())
        val result = seal.flatMap<Int, Byte> { throw mappedValue }

        // recycles the same instance for efficiency (starts as failure, is only cast to other result)
        assertThat(result).isSameInstanceAs(seal)
        assertThat(result).isEqualTo(seal)
        assertThat(result.throwableOrNull()).isNotEqualTo(mappedValue)
    }

    @Test
    fun `flatMapSealed catches throwable when success`() {
        val throwable = NotImplementedError()
        val seal = Seal.success(nextInt())
        val result = seal.flatMapSealed<Int, Byte> { throw throwable }

        assertThat(seal).isNotSameInstanceAs(result)
        assertThat(seal).isNotEqualTo(result)
        assertThat(result.throwableOrNull()).isSameInstanceAs(throwable)
    }

    @Test
    fun `flatMapSealed keeps throwable when failure`() {
        val mappedValue = IllegalArgumentException()
        val seal = Seal.failure<Int>(NotImplementedError())
        val result = seal.flatMapSealed<Int, Byte> { throw mappedValue }

        // recycles the same instance for efficiency (starts as failure, is only cast to other result)
        assertThat(result).isSameInstanceAs(seal)
        assertThat(result.throwableOrNull()).isNotEqualTo(mappedValue)
    }

    @Test
    fun `flatten returns first throwable`() {
        val mappedValue = IllegalArgumentException()
        val seals = listOf(
            Seal.success(1),
            Seal.failure(mappedValue),
            Seal.failure(NotImplementedError()),
        )
        val result = seals.flatten()

        assertThat(result.throwableOrNull()).isEqualTo(mappedValue)
    }

    @Test
    fun `flatten returns list without throwables`() {
        val seals = listOf(
            Seal.success(1),
            Seal.success(2),
            Seal.success(3),
        )
        val result = seals.flatten()

        assertThat(result.getOrNull()).containsExactly(1, 2, 3)
    }

    @Test
    fun `listMap updates value when success`() {
        val mappedValue = nextBytes(30)
        val seal = Seal.success(listOf(nextInt()))
        val result = seal.listMap { mappedValue }

        assertThat(result.getOrNull()).hasSize(1)
        assertThat(result.getOrNull()).containsExactly(mappedValue)
    }

    @Test
    fun `listMap keeps throwable when failure`() {
        val mappedValue = IllegalArgumentException()
        val seal = Seal.failure<List<Int>>(NotImplementedError())
        val result = seal.listMap<Int, Byte> { throw mappedValue }

        // recycles the same instance for efficiency (starts as failure, is only cast to other result)
        assertThat(result).isSameInstanceAs(seal)
        assertThat(result).isEqualTo(seal)
        assertThat(result.throwableOrNull()).isNotEqualTo(mappedValue)
    }

    @Test
    fun `listMapSealed catches throwable when success`() {
        val throwable = NotImplementedError()
        val seal = Seal.success(listOf(nextInt()))
        val result = seal.listMapSealed<Int, Byte> { throw throwable }

        assertThat(seal).isNotSameInstanceAs(result)
        assertThat(seal).isNotEqualTo(result)
        assertThat(result.throwableOrNull()).isSameInstanceAs(throwable)
    }

    @Test
    fun `listMapSealed keeps throwable when failure`() {
        val mappedValue = IllegalArgumentException()
        val seal = Seal.failure<List<Int>>(NotImplementedError())
        val result = seal.listMapSealed<Int, Byte> { throw mappedValue }

        // recycles the same instance for efficiency (starts as failure, is only cast to other result)
        assertThat(result).isSameInstanceAs(seal)
        assertThat(result).isEqualTo(result)
        assertThat(result.throwableOrNull()).isNotEqualTo(mappedValue)
    }

    @Test
    fun `fold returns onSuccess result when success`() {
        val mappedValue = nextBytes(30)
        val seal = Seal.success(nextInt())
        val result = seal.fold(
            onSuccess = { mappedValue },
            onFailure = { throw it }
        )

        assertThat(result).isSameInstanceAs(mappedValue)
    }

    @Test
    fun `fold returns onFailure result when failure`() {
        val mappedValue = IllegalArgumentException()
        val seal = Seal.failure<Int>(NotImplementedError())
        val result = seal.fold(
            onSuccess = { throw IllegalStateException() },
            onFailure = { mappedValue }
        )

        assertThat(result).isSameInstanceAs(mappedValue)
    }

    @Test
    fun `onFailureReturn does not return result of body when success`() {
        val input = 1
        val seal = Seal.success(input)
        val result = seal.onFailureReturn { 0 }

        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `onFailureReturn does not return default when success`() {
        val input = 1
        val seal = Seal.success(input)
        val result = seal.onFailureReturn(0)

        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `onFailureReturn returns result of body when failure`() {
        val seal = Seal.failure<Int>(Throwable())
        val result = seal.onFailureReturn { 0 }

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `onFailureReturn returns default when failure`() {
        val seal = Seal.failure<Int>(Throwable())
        val result = seal.onFailureReturn(0)

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `onSuccess invokes with value when success`() {
        val input = nextInt()
        val seal = Seal.success(input)
        var called = false
        seal.onSuccess {
            called = true
            assertThat(it).isEqualTo(input)
        }
        assertThat(called).isTrue()
    }

    @Test
    fun `onSuccess does not invoke with value when failure`() {
        val seal = Seal.failure<Int>(NotImplementedError())
        var called = false
        seal.onSuccess {
            called = true
            assert(false)
        }
        assertThat(called).isFalse()
    }

    @Test
    fun `onFailure invokes with throwable when failure`() {
        val input = NotImplementedError()
        val seal = Seal.failure<Int>(input)
        var called = false
        seal.onFailure {
            called = true
            assertThat(it).isEqualTo(input)
        }
        assertThat(called).isTrue()
    }

    @Test
    fun `onFailure does not invoke with throwable when success`() {
        val seal = Seal.success(nextInt())
        var called = false
        seal.onFailure {
            called = true
            assert(false)
        }
        assertThat(called).isFalse()
    }

}