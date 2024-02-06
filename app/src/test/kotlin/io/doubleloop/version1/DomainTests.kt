package io.doubleloop.version1

import io.doubleloop.version1.Command.*
import io.doubleloop.version1.Orientation.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DomainTests {

    @Test
    fun `turn right command`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 0), N)
        val command = TurnRight

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 0), E))
    }

    @Test
    fun `turn left command`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 0), N)
        val command = TurnLeft

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 0), W))
    }

    @Test
    fun `move forward command`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 1), N)
        val command = MoveForward

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 2), N))
    }

    @Test
    fun `move forward command, opposite orientation`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 1), S)
        val command = MoveForward

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 0), S))
    }

    @Test
    fun `move backward command`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 1), N)
        val command = MoveBackward

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 0), N))
    }

    @Test
    fun `move backward command, opposite orientation`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 1), S)
        val command = MoveBackward

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 2), S))
    }

    @Test
    fun `wrap on North`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 3), N)
        val command = MoveForward

        val result = command.execute(planet, rover)

        expectThat(result).isEqualTo(Rover(Position(0, 0), N))
    }

    @Test
    fun `go to opposite angle`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 0), N)
        val commands = listOf(TurnLeft, MoveForward, TurnRight, MoveBackward)

        val result = executeAll(planet, rover, commands)

        expectThat(result).isEqualTo(Rover(Position(4, 3), N))
    }
}