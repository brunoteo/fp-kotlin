package io.doubleloop.version3

import arrow.core.left
import arrow.core.right
import io.doubleloop.version3.Command.*
import io.doubleloop.version3.Orientation.E
import io.doubleloop.version3.Orientation.N
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

         expectThat(result).isEqualTo(Rover(Position(0, 0), E).right())
    }

    @Test
    fun `move forward command`() {
        val planet = Planet(Size(5, 4), listOf())
        val rover = Rover(Position(0, 1), N)
        val command = MoveForward

        val result = command.execute(planet, rover)

         expectThat(result).isEqualTo(Rover(Position(0, 2), N).right())
    }

    @Test
    fun `hit an obstacle`() {
        val planet = Planet(Size(5, 4), listOf(Obstacle(0, 2)))
        val rover = Rover(Position(0, 1), N)
        val command = MoveForward

        val result = command.execute(planet, rover)

         expectThat(result).isEqualTo(Rover(Position(0, 1), N).left())
    }

    @Test
    fun `many commands`() {
        val planet = Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3), Obstacle(3, 2)))
        val rover = Rover(Position(0, 0), N)
        val commands = listOf(TurnLeft, MoveForward, TurnRight, MoveBackward)

        val result = executeAll(planet, rover, commands)

        expectThat(result).isEqualTo(Rover(Position(4, 3), N).right())
    }

    @Test
    fun `many commands then hit an obstacle`() {
        val planet = Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3), Obstacle(3, 2)))
        val rover = Rover(Position(0, 0), N)
        val commands = listOf(TurnRight, MoveForward, MoveForward)

        val result = executeAll(planet, rover, commands)

        expectThat(result).isEqualTo(Rover(Position(1, 0), E).left())
    }
}