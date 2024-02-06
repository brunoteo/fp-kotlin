package io.doubleloop

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.right
import arrow.core.some
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ChainingTests {

    data class ItemId(val id: Int)
    data class Item(val id: ItemId, val qty: Int) {
        fun checkIn(qty: Int): Item =
            copy(qty = this.qty + qty)
    }

    @Test
    fun `chaining with Option`() {
        // NOTE: stub implementations
        //  just to make the compiler happy
        fun load(id: ItemId): Option<Item> =
            Item(id, 100).some()

        fun save(item: Item): Option<Unit> =
            Unit.some()

        val program: Option<Unit> =
            ItemId(123)
                .let(::load)
                .map { it.checkIn(10) }
                .flatMap(::save)
    }

    @Test
    fun `chaining with Either`() {
        // NOTE: stub implementations
        //  just to make the compiler happy
        fun load(id: ItemId): Either<String, Item> =
            Item(id, 100).right()

        fun save(item: Item): Either<String, Unit> =
            Unit.right()

        val program: Either<String, Unit> =
            ItemId(123)
                .let(::load)
                .map { it.checkIn(10) }
                .flatMap(::save)
    }
}