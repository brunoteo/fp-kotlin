package io.doubleloop.version1

/*
    ## V1 - Focus on domain logic

    Develop the functions that executes commands:
    - Implement all commands logic (left, right, forward and backward).
    - Commands are sent in batch and executed sequentially, one by one.
    - The planet grid has a wrapping effect from one edge to another (pacman).
    - For now, ignore obstacle detection logic
 */


sealed class Orientation {
    data object N : Orientation()
    data object E : Orientation()
    data object W : Orientation()
    data object S : Orientation()

    fun turnRight(): Orientation =
        when (this) {
            E -> S
            N -> E
            S -> W
            W -> N
        }

    fun turnLeft(): Orientation =
        when (this) {
            E -> N
            N -> W
            S -> E
            W -> S
        }
}

data class Position(val x: Int, val y: Int)

data class Rover(val position: Position, val orientation: Orientation) {
    fun turnRight(): Rover =
        copy(orientation = orientation.turnRight())

    fun turnLeft(): Rover =
        copy(orientation = orientation.turnLeft())

    fun moveForward(planet: Planet): Rover =
        when (this.orientation) {
            Orientation.E -> position.copy(x = position.x + 1)
            Orientation.N -> position.copy(y = position.y + 1)
            Orientation.S -> position.copy(y = position.y - 1)
            Orientation.W -> position.copy(x = position.x - 1)
        }.let {
            copy(position = planet.wrap(it))
        }

    fun moveBackward(planet: Planet): Rover =
        when (this.orientation) {
            Orientation.E -> position.copy(x = position.x - 1)
            Orientation.N -> position.copy(y = position.y - 1)
            Orientation.S -> position.copy(y = position.y + 1)
            Orientation.W -> position.copy(x = position.x + 1)
        }.let {
            copy(position = planet.wrap(it))
        }
}


data class Size(val width: Int, val height: Int)
data class Obstacle(val x: Int, val y: Int)
data class Planet(val size: Size, val obstacles: List<Obstacle>) {
    fun wrap(candidate: Position): Position =
        candidate.copy(
            x = wrap(candidate.x, size.width),
            y = wrap(candidate.y, size.height)
        )

    // NOTE: utility function for the pacman effect
    private fun wrap(value: Int, limit: Int): Int =
        ((value % limit) + limit) % limit
}

sealed class Command {
    data object MoveForward : Command()
    data object MoveBackward : Command()
    data object TurnRight : Command()
    data object TurnLeft : Command()

    fun execute(planet: Planet, rover: Rover): Rover =
        when (this) {
            MoveBackward -> rover.moveBackward(planet)
            MoveForward -> rover.moveForward(planet)
            TurnLeft -> rover.turnLeft()
            TurnRight -> rover.turnRight()
        }
}

fun executeAll(planet: Planet, rover: Rover, commands: List<Command>): Rover =
    commands.fold(rover) { acc, command ->
        command.execute(planet, acc)
    }