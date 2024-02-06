package io.doubleloop

import io.doubleloop.CreationPhaseTests.OptionalItem.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CreationPhaseTests {

    data class Item(val qty: Int)

    sealed class OptionalItem {
        data class Valid(val item: Item) : OptionalItem()
        data object Invalid : OptionalItem()
    }

    fun parseItem(qty: String): OptionalItem =
        if (qty.matches(Regex("^[0-9]+$"))) Valid(Item(qty.toInt()))
        else Invalid

    @Test
    fun `item creation`() {
        val result = parseItem("10")

         expectThat(result).isEqualTo(Valid(Item(10)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["asd", "1 0 0", ""])
    fun `invalid item creation`(input: String) {
        val result = parseItem(input)

         expectThat(result).isEqualTo(Invalid)
    }
}