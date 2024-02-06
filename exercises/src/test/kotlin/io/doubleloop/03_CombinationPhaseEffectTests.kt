package io.doubleloop

import arrow.core.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

// TODO 1: remove the disabled annotation and make all tests green
class CombinationPhaseEffectTests {

    data class Item(val qty: Int) {
        fun checkIn(qty: Int): Item =
            copy(qty = this.qty + qty)

        fun checkOut(qty: Int): Option<Item> =
            if (this.qty - qty < 0) none()
            else copy(qty = this.qty - qty).some()
    }

    fun parseItem(qty: String): Option<Item> =
        if (qty.matches(Regex("^[0-9]+$"))) Item(qty.toInt()).some()
        else none()

    @Test
    fun `creation and checkOut`() {
        val item = parseItem("100")

        val result = item.flatMap {
            it.checkOut(10)
        }

        expectThat(result).isEqualTo(Some(Item(90)))
    }

    @Test
    fun `creation and invalid checkOut`() {
        val item = parseItem("10")

        val result = item.flatMap {
            it.checkOut(110)
        }

        expectThat(result).isEqualTo(None)
    }

    @ParameterizedTest
    @ValueSource(strings = ["asd", "1 0 0", ""])
    fun `invalid creation and checkOut`(input: String) {
        val item = parseItem(input)

        val result = item.flatMap {
            it.checkOut(10)
        }

        expectThat(result).isEqualTo(None)
    }

    @Test
    fun `creation, checkIn and checkOut`() {
        val item = parseItem("100")

        val result = item
            .map { it.checkIn(10) }
            .flatMap { it.checkOut(20) }

        expectThat(result).isEqualTo(Some(Item(90)))
    }
}