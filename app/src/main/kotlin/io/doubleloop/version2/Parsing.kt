package io.doubleloop.version2

/*
    ## V2 - Focus on boundaries (from primitive to domain types and vice versa)

    Our domain is declared with rich types but inputs/outputs are primitive types
    - Write a parser for input planet data (size, obstacles)
    - Write a parser for input rover data (position, orientation)
    - Write a parser for input commands
    - Render the final result as string: "positionX:positionY:orientation"
 */

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.doubleloop.version2.ParseError.InvalidCommand
import io.doubleloop.version2.ParseError.InvalidPlanet
import io.doubleloop.version2.ParseError.InvalidRover

sealed class ParseError {
    data class InvalidPlanet(val message: String) : ParseError()
    data class InvalidRover(val message: String) : ParseError()
    data class InvalidCommand(val message: String) : ParseError()
}

// NOTE: utility function to split a string in a pair of numbers given a separator
// EXAMPLE USAGE:
//  parsePair("-", "1-0") == Right((1, 0))
//  parsePair("-", "10") == Left(Exception(...))
//  parsePair("-", "1, 0") == Left(Exception(...))
fun parsePair(separator: String, input: String): Either<Throwable, Pair<Int, Int>> =
    Either.catch {
        val parts = input.split(separator)
        val first = parts[0].trim().toInt()
        val second = parts[1].trim().toInt()
        Pair(first, second)
    }

// INPUT EXAMPLE: "2,0" -> Right(Position(2, 0))
// INPUT EXAMPLE: "20" -> Left(InvalidPlanet("Invalid position"))
// HINT: combination phase normal (Functor)
fun parsePosition(input: String): Either<ParseError, Position> =
    parsePair(",", input)
        .mapLeft { InvalidRover("invalid position: $input") }
        .map { Position(it.first, it.second) }

// INPUT EXAMPLE: "N" -> Right(N)
// INPUT EXAMPLE: "X" -> Left(InvalidPlanet("Invalid orientation"))
// HINT: creation phase
fun parseOrientation(input: String): Either<ParseError, Orientation> =
    when (input.uppercase()) {
        "N" -> Orientation.N.right()
        "E" -> Orientation.E.right()
        "W" -> Orientation.W.right()
        "S" -> Orientation.S.right()
        else -> InvalidRover("invalid orientation: $input").left()
    }

// INPUT EXAMPLE: ("2,0", "N") -> Right(Rover(Position(2, 0), N))
// HINT: combination phase many (Applicative)
fun parseRover(input: Pair<String, String>): Either<ParseError, Rover> =
    either {
        val position = parsePosition(input.first).bind()
        val orientation = parseOrientation(input.second).bind()
        Rover(position, orientation)
    }

// INPUT EXAMPLE: "5x4" -> Right(Size(5, 4))
// HINT: combination phase normal (Functor)
fun parseSize(input: String): Either<ParseError, Size> =
    parsePair("x", input)
        .mapLeft { InvalidPlanet("invalid size: $input") }
        .map { Size(it.first, it.second) }

// INPUT EXAMPLE: "2,0" -> Right(Obstacle(2, 0))
// HINT: combination phase normal (Functor)
fun parseObstacle(input: String): Either<ParseError, Obstacle> =
    parsePair(",", input)
        .mapLeft { InvalidPlanet("Invalid obstacle") }
        .map { Obstacle(it.first, it.second) }

// INPUT EXAMPLE: "2,0 0,3" -> Right(listOf(Obstacle(2, 0), Obstacle(0, 3)))
// HINT: combination phase list (Traversal)
fun parseObstacles(input: String): Either<ParseError, List<Obstacle>> =
    input.split(" ")
        .map { parseObstacle(it) }
        .let { either { it.bindAll() } }
        .mapLeft { InvalidPlanet("invalid obstacles: $input") }

fun parseObstaclesAlternative(input: String): Either<ParseError, List<Obstacle>> =
    either {
        input.split(" ")
            .map { parseObstacle(it).bind() }
    }

// INPUT EXAMPLE: ("5x4", "2,0 0,3") -> Right(Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3))))
// HINT: combination phase many (Applicative)
fun parsePlanet(input: Pair<String, String>): Either<ParseError, Planet> =
    either {
        val size = parseSize(input.first).bind()
        val obstacles = parseObstacles(input.second).bind()
        Planet(size, obstacles)
    }

// INPUT EXAMPLE: "B" -> Right(MoveBackward)
// INPUT EXAMPLE: "X" -> Left(InvalidCommand("Invalid command"))
// HINT: creation phase
fun parseCommand(input: Char): Either<ParseError, Command> =
    when (input.uppercase()) {
        "B" -> Command.MoveBackward.right()
        "F" -> Command.MoveForward.right()
        "L" -> Command.TurnLeft.right()
        "R" -> Command.TurnRight.right()
        else -> InvalidCommand("invalid command: $input").left()
    }

// INPUT EXAMPLE: "BFLR" -> Right(listOf(MoveBackward, MoveForward, TurnLeft, TurnRight))
// INPUT EXAMPLE: "BFXLR" -> Left(InvalidCommand("Invalid command"))
// HINT: combination phase list (Traversal)
fun parseCommands(input: String): Either<ParseError, List<Command>> =
    either {
        input.toCharArray()
            .map { parseCommand(it).bind() }
    }

// HINT: combination phase many (Applicative)
fun runMission(
    inputPlanet: Pair<String, String>,
    inputRover: Pair<String, String>,
    inputCommands: String
): Either<ParseError, Rover> =
    either {
        val planet = parsePlanet(inputPlanet).bind()
        val rover = parseRover(inputRover).bind()
        val commands = parseCommands(inputCommands).bind()
        executeAll(planet, rover, commands)
    }

// OUTPUT EXAMPLE: Rover(Position(3, 2), N) -> "3:2:S"
fun renderComplete(rover: Rover): String =
    "${rover.position.x}:${rover.position.y}:${rover.orientation}"

fun runApp(
    inputPlanet: Pair<String, String>,
    inputRover: Pair<String, String>,
    inputCommands: String
): Either<ParseError, String> =
    runMission(inputPlanet, inputRover, inputCommands)
        .map { renderComplete(it) }