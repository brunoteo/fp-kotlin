package io.doubleloop.version3

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.doubleloop.version3.Orientation.*
import io.doubleloop.version3.ParseError.*

sealed class ParseError {
    data class InvalidPlanet(val message: String) : ParseError()
    data class InvalidRover(val message: String) : ParseError()
    data class InvalidCommand(val message: String) : ParseError()
}

fun renderComplete(rover: Rover): String =
    "${rover.position.x}:${rover.position.y}:${rover.orientation}"

// HINT: format "O:positionX:positionY:orientation"
fun renderObstacle(rover: ObstacleDetected): String =
    "O:${renderComplete(rover)}"

// HINT: keep the parsing Either
// HINT: use proper renderObstacle or renderComplete to produce the final string
// HINT: combine phase normal (Functor) and then removal phase
fun runApp(
    inputPlanet: Pair<String, String>,
    inputRover: Pair<String, String>,
    inputCommands: String
): Either<ParseError, String> =
    runMission(inputPlanet, inputRover, inputCommands)
        .map { renderMission(it) }

private fun renderMission(rover: Either<ObstacleDetected, Rover>) = rover.fold(
    { renderObstacle(it) },
    { renderComplete(it) }
)

fun runMission(
    inputPlanet: Pair<String, String>,
    inputRover: Pair<String, String>,
    inputCommands: String
): Either<ParseError, Either<ObstacleDetected, Rover>> = either {
    val planet = parsePlanet(inputPlanet).bind()
    val rover = parseRover(inputRover).bind()
    val commands = parseCommands(inputCommands).bind()
    executeAll(planet, rover, commands)
}

fun parseCommand(input: Char): Either<ParseError, Command> =
    when (input.lowercase()) {
        "f" -> Command.MoveForward.right()
        "b" -> Command.MoveBackward.right()
        "r" -> Command.TurnRight.right()
        "l" -> Command.TurnLeft.right()
        else -> InvalidCommand("invalid command: $input").left()
    }

fun parseCommands(input: String): Either<ParseError, List<Command>> = either {
    input
        .trim()
        .map { parseCommand(it).bind() }
}

fun parsePosition(input: String): Either<ParseError, Position> =
    parsePair(",", input)
        .map { Position(it.first, it.second) }
        .mapLeft { InvalidRover("invalid position: $input") }

fun parseOrientation(input: String): Either<ParseError, Orientation> =
    when (input.trim().lowercase()) {
        "n" -> N.right()
        "w" -> W.right()
        "e" -> E.right()
        "s" -> S.right()
        else -> InvalidRover("invalid orientation: $input").left()
    }

fun parseRover(input: Pair<String, String>): Either<ParseError, Rover> = either {
    val (inputPosition, inputOrientation) = input
    val position = parsePosition(inputPosition).bind()
    val orientation = parseOrientation(inputOrientation).bind()
    Rover(position, orientation)
}

fun parseSize(input: String): Either<ParseError, Size> =
    parsePair("x", input)
        .map { Size(it.first, it.second) }
        .mapLeft { InvalidPlanet("invalid size: $input") }

fun parseObstacle(input: String): Either<ParseError, Obstacle> =
    parsePair(",", input)
        .map { Obstacle(it.first, it.second) }
        .mapLeft { InvalidPlanet("invalid obstacle: $input") }

fun parseObstacles(input: String): Either<ParseError, List<Obstacle>> = either {
    input
        .split(" ")
        .map { parseObstacle(it).bind() }
}
    .mapLeft { InvalidPlanet("invalid obstacles: $input") }

fun parsePlanet(input: Pair<String, String>): Either<ParseError, Planet> = either {
    val (inputSize, inputObstacles) = input
    val size = parseSize(inputSize).bind()
    val obstacles = parseObstacles(inputObstacles).bind()
    Planet(size, obstacles)
}

fun parsePair(separator: String, input: String): Either<Throwable, Pair<Int, Int>> =
    Either.catch {
        val parts = input.split(separator)
        val first = parts[0].trim().toInt()
        val second = parts[1].trim().toInt()
        Pair(first, second)
    }
