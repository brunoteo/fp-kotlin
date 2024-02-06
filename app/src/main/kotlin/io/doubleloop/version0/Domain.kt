package io.doubleloop.version0

/*
    ## V0 - Focus on domain types

    Describe the domain types using:
      - native types, typealias, type wrappers, product and sum types.
 */

// TODO 1: Those type alias are only placeholders,  use correct/better type definitions.
//  Remember to be as strict as possible but also to keep it simple.

data class Rover(val position: Position, val orientation: Orientation)
data class Position(val x: Int, val y: Int)
sealed class Orientation {
    data object North: Orientation()
    data object South: Orientation()
    data object West: Orientation()
    data object East: Orientation()
}
enum class Orientation2 {
    NORTH,
    SOUTH,
    WEST,
    EST
}

data class Planet(val size: Size, val obstacles: List<Obstacle>)
data class Size(val width: Int, val height: Int)
data class Obstacle(val position: Position)

sealed class Command {
    data object Left: Command()
    data object Right: Command()
    data object Forward: Command()
    data object Backward: Command()
}

enum class Command2 {
    LEFT,
    RIGHT,
    FORWARD,
    BACKWARD
}