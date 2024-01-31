package io.doubleloop.solutions.version5

import io.doubleloop.solutions.utils.Console
import io.doubleloop.solutions.version5.Command.*
import io.doubleloop.solutions.version5.Orientation.N
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class InfraTests {

    @Test
    fun `go to opposite angle (integration)`() = runTest {
        val result = runCaptureOutput("RBBLBRF") {
            createApplication("planet.txt", "rover.txt")
        }
        expectThat(result).isEqualTo("${Console.GREEN}[OK] 4:3:E${Console.RESET}")
    }

    @Test
    fun `go to opposite angle`() = runTest {
        val missionSource = StubMissionSource(
            Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3), Obstacle(3, 2))),
            Rover(Position(0, 0), N)
        )
        val commandsChannel = StubCommandsChannel(
            listOf(
                TurnRight,
                MoveBackward,
                MoveBackward,
                TurnLeft,
                MoveBackward,
                TurnRight,
                MoveForward
            )
        )
        val missionReport = SpyMissionReport()

        createApplication(missionSource, commandsChannel, missionReport)

        expectThat(missionReport.output).isEqualTo("COMPLETED: Rover(position=Position(x=4, y=3), orientation=E)")
    }

    @Test
    fun `hit obstacle during commands execution`() = runTest {
        val missionSource = StubMissionSource(
            Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3), Obstacle(3, 2))),
            Rover(Position(0, 0), N)
        )
        val commandsChannel = StubCommandsChannel(
            listOf(
                TurnRight,
                MoveForward,
                MoveForward
            )
        )
        val missionReport = SpyMissionReport()

        createApplication(missionSource, commandsChannel, missionReport)

        expectThat(missionReport.output).isEqualTo("OBSTACLE: Rover(position=Position(x=1, y=0), orientation=E)")
    }

    private class StubMissionSource(val planet: Planet, val rover: Rover) : MissionSource {
        override suspend fun readPlanet(): Planet = planet
        override suspend fun readRover(): Rover = rover
    }

    private class StubCommandsChannel(val commands: List<Command>) : CommandsChannel {
        override suspend fun receive(): List<Command> = commands
    }

    private class SpyMissionReport(var output: String = "") : MissionReport {
        override suspend fun sequenceCompleted(rover: Rover) {
            output = "COMPLETED: $rover"
        }

        override suspend fun obstacleDetected(rover: ObstacleDetected) {
            output = "OBSTACLE: $rover"
        }

        override suspend fun error(error: Throwable) {
            output = "ERROR: $error"
        }
    }

    private suspend fun runCaptureOutput(commands: String, program: suspend () -> Unit): String {
        val originalIn = System.`in`
        val originalOut = System.out

        try {
            val input = ByteArrayInputStream("$commands\n".toByteArray())
            val output = ByteArrayOutputStream()

            System.setIn(input)
            System.setOut(PrintStream(output))

            program.invoke()
            return output.toString()
                .replace("\r", "")
                .replace("Waiting commands...\n", "")
                .split('\n')
                .first()
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
        }
    }

}