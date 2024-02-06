package io.doubleloop

import arrow.core.Option
import arrow.core.none
import arrow.core.raise.option
import arrow.core.some
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CombinationPhaseListTests {

    data class Item(val qty: Int)

    fun parseItem(qty: String): Option<Item> =
        if (qty.matches(Regex("^[0-9]+$"))) Item(qty.toInt()).some()
        else none()

    @Test
    fun `all valid - list of results`() {
        val items: List<Option<Item>> = listOf("1", "10", "100")
            .map { parseItem(it) }

        expectThat(items).isEqualTo(
            listOf(Item(1).some(), Item(10).some(), Item(100).some())
        )
    }

    @Test
    fun `some invalid - list of results`() {
        val items: List<Option<Item>> = listOf("1", "asd", "100")
            .map { parseItem(it) }

        expectThat(items).isEqualTo(
            listOf(Item(1).some(), none(), Item(100).some())
        )
    }

    @Test
    fun `all valid - result of list`() {
        val items: Option<List<Item>> = option {
            listOf("1", "10", "100")
                .map { parseItem(it).bind() }
        }

        expectThat(items).isEqualTo(
            listOf(Item(1), Item(10), Item(100)).some()
        )
    }

    @Test
    fun `some invalid - result of list`() {
        val items: Option<List<Item>> = option {
            listOf("1", "asd", "100")
                .map { parseItem(it).bind() }
        }

        expectThat(items).isEqualTo(
            none()
        )
    }
}