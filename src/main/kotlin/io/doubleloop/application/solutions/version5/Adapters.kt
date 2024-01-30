package io.doubleloop.application.solutions.version5

class FileMissionSource(private val planetFile: String, private val roverFile: String) : MissionSource {
    override suspend fun readPlanet(): Planet =
        loadPlanet(planetFile)

    override suspend fun readRover(): Rover =
        loadRover(roverFile)
}

class ConsoleCommandsChannel() : CommandsChannel {
    override suspend fun receive(): List<Command> =
        loadCommands()
}

class ConsoleMissionReport() : MissionReport {
    override suspend fun sequenceCompleted(rover: Rover) =
        writeSequenceCompleted(rover)

    override suspend fun obstacleDetected(rover: ObstacleDetected) =
        writeObstacleDetected(rover)

    override suspend fun error(error: Throwable) =
        writeError(error)
}