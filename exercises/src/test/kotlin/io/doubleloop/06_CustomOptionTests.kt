package io.doubleloop

import arrow.core.identity
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CustomOptionTests {

    fun increment(x: Int): Int =
        x + 1

    fun string(x: Int): Option<String> =
        Option.of(x.toString())

    @Test
    fun `creation phase`() {
        val result = Option.of(10)

        expectThat(result).isEqualTo(Option.Some(10))
    }

    @Test
    fun `combination phase - normal`() {
        val result = Option.of(10)
            .map { increment(it) }

        expectThat(result).isEqualTo(Option.Some(11))
    }

    @Test
    fun `combination phase - effect`() {
        val result = Option.of(10)
            .flatMap { string(it) }

        expectThat(result).isEqualTo(Option.Some("10"))
    }

    @Test
    fun `removal phase - value`() {
        val result = Option.of(10)
            .fold({ "empty" }, { "value: $it" })

        expectThat(result).isEqualTo("value: 10")
    }

    @Test
    fun `removal phase - alternative value`() {
        val result = Option.none<Int>()
            .fold({ "empty" }, { "value: $it" })

        expectThat(result).isEqualTo("empty")
    }

    @Test
    fun `functor laws - identity`() {
        val result = Option.of(10)
            .map { identity(it) }
        val expected = Option.of(identity(10))
        expectThat(result).isEqualTo(expected)
    }

    @Test
    fun `functor laws - composition`() {
        val result = Option.of(10)
            .map { increment(it) }
            .map { increment(it) }
        val expected = Option.of(10)
            .map { increment(increment(it)) }
        expectThat(result).isEqualTo(expected)
    }

    @Test
    fun `monad laws - left identity`() {
        val result = Option.of(10)
            .flatMap { string(it) }
        val expected = string(10)
        expectThat(result).isEqualTo(expected)
    }

    @Test
    fun `monad laws - right identity`() {
        val result = Option.of(10)
            .flatMap { Option.of(it) }
        val expected = Option.of(10)
        expectThat(result).isEqualTo(expected)
    }

    @Test
    fun `monad laws - associativity`() {
        val result = Option.of(10)
            .flatMap { Option.of(it) }
            .flatMap { string(it) }
        val expected = Option.of(10)
            .flatMap { x -> Option.of(x).flatMap { string(it) } }
        expectThat(result).isEqualTo(expected)
    }

    sealed class Option<out A> {
        data class Some<A>(val value: A) : Option<A>()
        data object None : Option<Nothing>()

        companion object {
            fun <A> of(value: A): Option<A> =
                Some(value)

            fun <A> none(): Option<A> =
                None
        }

        fun <B> map(f: (A) -> B): Option<B> =
            when (this) {
                is Some -> Some(f(this.value))
                is None -> None
            }

        fun <B> flatMap(f: (A) -> Option<B>): Option<B> =
            when (this) {
                is Some -> f(this.value)
                is None -> None
            }

        fun <B> fold(ifEmpty: () -> B, ifSome: (A) -> B): B =
            when (this) {
                is Some -> ifSome(this.value)
                is None -> ifEmpty()
            }
    }
}