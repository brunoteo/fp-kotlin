package io.doubleloop.version5.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.doubleloop.version5.domain.Orientation.*

data class Obstacle(val x: Int, val y: Int)
data class Planet(val size: Size, val obstacles: List<Obstacle>) {
    fun wrap(candidate: Position): Position =
        candidate.copy(
            x = wrap(candidate.x, size.width),
            y = wrap(candidate.y, size.height)
        )

    private fun wrap(value: Int, limit: Int): Int =
        ((value % limit) + limit) % limit
}

data class Size(val width: Int, val height: Int)
data class Position(val x: Int, val y: Int) {

    fun shift(delta: Delta): Position {
        return copy(
            x = x + delta.x,
            y = y + delta.y
        )
    }
}

data class Rover(val position: Position, val orientation: Orientation) {
    fun turnRight(): Rover =
        copy(orientation = orientation.turnRight())

    fun turnLeft(): Rover =
        copy(orientation = orientation.turnLeft())

    fun moveForward(planet: Planet): Either<ObstacleDetected, Rover> =
        next(planet, delta(orientation))
            .map { x -> copy(position = x) }

    fun moveBackward(planet: Planet): Either<ObstacleDetected, Rover> =
        next(planet, delta(orientation.opposite()))
            .map { x -> copy(position = x) }

    private fun next(planet: Planet, delta: Delta): Either<ObstacleDetected, Position> {
        val candidate = planet.wrap(position.shift(delta))
        val hitObstacle = planet.obstacles.any { Position(it.x, it.y) == candidate }
        return if (hitObstacle) obstacleDetected().left() else candidate.right()
    }

    private fun delta(orientation: Orientation): Delta =
        when (orientation) {
            is N -> Delta(0, 1)
            is S -> Delta(0, -1)
            is E -> Delta(1, 0)
            is W -> Delta(-1, 0)
        }

    private fun obstacleDetected(): ObstacleDetected = this

}
typealias ObstacleDetected = Rover
data class Delta(val x: Int, val y: Int)

sealed class Command {
    data object MoveForward : Command()
    data object MoveBackward : Command()
    data object TurnRight : Command()
    data object TurnLeft : Command()

    fun execute(planet: Planet, rover: Rover): Either<ObstacleDetected, Rover> =
        when (this) {
            is TurnRight -> rover.turnRight().right()
            is TurnLeft -> rover.turnLeft().right()
            is MoveForward -> rover.moveForward(planet)
            is MoveBackward -> rover.moveBackward(planet)
        }
}

sealed class Orientation {
    data object N : Orientation()
    data object E : Orientation()
    data object W : Orientation()
    data object S : Orientation()

    fun turnRight(): Orientation =
        when (this) {
            is N -> E
            is E -> S
            is S -> W
            is W -> N
        }

    fun turnLeft(): Orientation =
        when (this) {
            is N -> W
            is W -> S
            is S -> E
            is E -> N
        }

    fun opposite(): Orientation =
        when (this) {
            is N -> S
            is S -> N
            is E -> W
            is W -> E
        }
}

fun executeAll(planet: Planet, rover: Rover, commands: List<Command>): Either<ObstacleDetected, Rover> {
    val initial: Either<ObstacleDetected, Rover> = rover.right()
    return commands.fold(initial) { prev, cmd ->
        prev.flatMap { cmd.execute(planet, it) }
    }
}