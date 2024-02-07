package io.doubleloop.version5.infrastructure

/*
    ## V5 - Testability via injection (Port/Adapter architectural style)

    Apply Dependency Inversion Principle (DIP) and Dependency Injection (DI) to our application
    - Look to the Ports for read planet, rover and commands
    - Implement port adapters
    - Define injectable application
    - Use test doubles in test suite
 */

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import io.doubleloop.utils.Console.ask
import io.doubleloop.utils.Console.logError
import io.doubleloop.utils.Console.logInfo
import io.doubleloop.utils.File.loadPair
import io.doubleloop.version5.domain.Command
import io.doubleloop.version5.domain.ObstacleDetected
import io.doubleloop.version5.domain.Planet
import io.doubleloop.version5.domain.Rover
import io.doubleloop.version5.domain.executeAll
import io.doubleloop.version5.domain.CommandsChannel
import io.doubleloop.version5.domain.MissionReport
import io.doubleloop.version5.domain.MissionSource
import kotlin.coroutines.suspendCoroutine

suspend fun runApp(planetFile: String, roverFile: String) {
    val fileMissionSource = FileMissionSource(planetFile, roverFile)
    val consoleCommandsChannel = ConsoleCommandsChannel()
    val consoleMissionReport = ConsoleMissionReport()
    runApp(fileMissionSource, consoleCommandsChannel, consoleMissionReport)
}

suspend fun runApp(
    missionSource: MissionSource,
    commandsChannel: CommandsChannel,
    missionReport: MissionReport
) {
    catch({
        runMission(missionSource, commandsChannel)
            .fold(
                { missionReport.obstacleDetected(it) },
                { missionReport.sequenceCompleted(it) }
            )
    }) { missionReport.error(it) }
}

suspend fun runMission(
    missionSource: MissionSource,
    commandsChannel: CommandsChannel
): Either<ObstacleDetected, Rover> {
    val planet = missionSource.readPlanet()
    val rover = missionSource.readRover()
    val commands = commandsChannel.receive()
    return executeAll(planet, rover, commands)
}

suspend fun loadPlanet(fileName: String): Planet = either {
    val input = loadPair(fileName)
    parsePlanet(input).bind()
}.toSuspend()

suspend fun loadRover(fileName: String): Rover = either {
    val input = loadPair(fileName)
    parseRover(input).bind()
}.toSuspend()

suspend fun loadCommands(): List<Command> = either {
    val input = ask("Waiting commands...")
    parseCommands(input).bind()
}.toSuspend()

suspend fun writeSequenceCompleted(rover: Rover) {
    logInfo(renderComplete(rover))
}

suspend fun writeObstacleDetected(rover: ObstacleDetected) {
    logInfo(renderObstacle(rover))
}

suspend fun writeError(error: Throwable) {
    logError(error.message ?: "Unknown error")
}

suspend fun <R> Either<ParseError, R>.toSuspend(): R = suspendCoroutine { continuation ->
    fold(
        { error -> continuation.resumeWith(Result.failure(RuntimeException(renderError(error)))) },
        { value -> continuation.resumeWith(Result.success(value)) }
    )
}