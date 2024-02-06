package io.doubleloop

import arrow.core.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CombinationPhaseNormalTests {

    data class Item(val qty: Int) {
        fun checkIn(qty: Int): Item =
            copy(qty = this.qty + qty)
    }

    fun parseItem(qty: String): Option<Item> =
        if (qty.matches(Regex("^[0-9]+$"))) Item(qty.toInt()).some()
        else none()

    @Test
    fun `creation and checkIn`() {

        val result = parseItem("10").map { it.checkIn(10) }

        expectThat(result).isEqualTo(Some(Item(20)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["asd", "1 0 0", ""])
    fun `invalid creation and checkIn`(input: String) {

        val result = parseItem(input).map { it.checkIn(10) }

        expectThat(result).isEqualTo(None)
    }
}