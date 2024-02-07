package io.doubleloop.version3

import arrow.core.left
import arrow.core.right
import io.doubleloop.version3.ParseError.InvalidPlanet
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ParsingTests {

    @Test
    fun `go to opposite angle`() {
        val planet = Pair("5x4", "2,0 0,3 3,2")
        val rover = Pair("0,0", "N")
        val commands = "RBBLBRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo("4:3:E".right())
    }

    @Test
    fun `hit obstacle during commands execution`() {
        val planet = Pair("5x4", "2,0 0,3 3,2")
        val rover = Pair("0,0", "N")
        val commands = "RFF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo("O:1:0:E".right())
    }

    @Test
    fun `invalid planet input data`() {
        val planet = Pair("ax4", "2,0 0,3 3,2")
        val rover = Pair("1,2", "N")
        val commands = "RBRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo(InvalidPlanet("invalid size: ax4").left())
    }
}