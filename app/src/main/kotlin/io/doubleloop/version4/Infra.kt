package io.doubleloop.version4

/*
    ## V4 - Focus on infrastructure (compose I/O operations)

    Extend the pure way of work also to the infrastructural layer
    - Read planet data from file (size and obstacles)
    - Read rover data from file (position and orientation)
    - Ask commands from the console
    - Implement an entrypoint that:
      - run the whole app
      - log final rover output to the console
      - log any unhandled error to the console
 */

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import io.doubleloop.utils.Console
import io.doubleloop.utils.Console.ask
import io.doubleloop.utils.Console.logError
import io.doubleloop.utils.Console.logInfo
import io.doubleloop.utils.File.loadPair
import kotlin.coroutines.suspendCoroutine

// HINT: combination phase effect (Monad)
// HINT: use either syntax
suspend fun loadPlanet(fileName: String): Planet = either {
    parsePlanet(loadPair(fileName)).bind()
}.toSuspend()

// HINT: combination phase effect (Monad)
// HINT: use either syntax
suspend fun loadRover(fileName: String): Rover = either {
    loadPair(fileName)
        .let(::parseRover)
        .bind()
}.toSuspend()

// HINT: combination phase effect (Monad)
// HINT: use either syntax
suspend fun loadCommands(): List<Command> = either {
    ask("Waiting commands...")
        .let(::parseCommands)
        .bind()
}.toSuspend()

suspend fun runMission(planetFile: String, roverFile: String): Either<ObstacleDetected, Rover> {
    val planet = loadPlanet(planetFile)
    val rover = loadRover(roverFile)
    val commands = loadCommands()
    return executeAll(planet, rover, commands)
}

suspend fun writeSequenceCompleted(rover: Rover) {
    logInfo(renderComplete(rover))
}

suspend fun writeObstacleDetected(rover: ObstacleDetected) {
    logInfo(renderObstacle(rover))
}

suspend fun writeError(error: Throwable) {
    val message = error.message ?: "Unknown error"
    logError(message)
}

// HINT: runMission returns TaskEither but runApp only Task
// HINT: combine phase normal and then removal phase

// HINT: combine phase normal and then removal phase
suspend fun runApp(planetFile: String, roverFile: String) {
    catch({
        runMission(planetFile, roverFile).fold(
            { writeObstacleDetected(it) },
            { writeSequenceCompleted(it) }
        )
    }) { writeError(it) }
}

suspend fun <R> Either<ParseError, R>.toSuspend(): R = suspendCoroutine { continuation ->
    fold(
        { error -> continuation.resumeWith(Result.failure(RuntimeException(renderError(error)))) },
        { value -> continuation.resumeWith(Result.success(value)) }
    )
}