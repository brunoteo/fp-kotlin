package io.doubleloop.version2

import arrow.core.left
import arrow.core.right
import io.doubleloop.version2.ParseError.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ParsingTests {

    @Test
    fun `parse valid size`() {
        val input = "5x4"

        val result = parseSize(input)

        expectThat(result).isEqualTo(Size(5, 4).right())
    }

    @ParameterizedTest
    @ValueSource(strings = ["a,0", "2,a", "2", ",", "apple"])
    fun `parse invalid size`(input: String) {
        val result = parseSize(input)

        expectThat(result).isEqualTo(InvalidPlanet("invalid size: $input").left())
    }

    @Test
    fun `parse valid obstacles`() {
        val input = "2,0 0,3"

        val result = parseObstacles(input)

        expectThat(result).isEqualTo(listOf(Obstacle(2, 0), Obstacle(0, 3)).right())
    }

    @ParameterizedTest
    @ValueSource(strings = ["20 0,3", "2,0 03", "apple"])
    fun `parse invalid obstacles`(input: String) {

        val result = parseObstacles(input)

        expectThat(result).isEqualTo(InvalidPlanet("invalid obstacles: $input").left())
    }

    @Test
    fun `go to opposite angle`() {
        val planet = Pair("5x4", "2,0 0,3 3,2")
        val rover = Pair("0,0", "N")
        val commands = "RBBLBRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo("4:3:E".right())
    }

    @Test
    fun `invalid planet input data`() {
        val planet = Pair("ax4", "2,0 0,3 3,2")
        val rover = Pair("1,2", "N")
        val commands = "RBRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo(InvalidPlanet("invalid size: ax4").left())
    }

    @Test
    fun `invalid rover input data`() {
        val planet = Pair("5x4", "2,0 0,3 3,2")
        val rover = Pair("1,2", "X")
        val commands = "RBRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo(InvalidRover("invalid orientation: X").left())
    }

    @Test
    fun `unknown command`() {
        val planet = Pair("5x4", "2,0 0,3 3,2")
        val rover = Pair("1,2", "N")
        val commands = "RBXRF"

        val result = runApp(planet, rover, commands)

        expectThat(result).isEqualTo(InvalidCommand("invalid command: X").left())
    }
}