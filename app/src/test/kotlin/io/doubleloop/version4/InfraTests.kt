package io.doubleloop.version4

import io.doubleloop.utils.Console
import io.doubleloop.version4.Command.MoveForward
import io.doubleloop.version4.Command.TurnRight
import io.doubleloop.version4.Orientation.N
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class InfraTests {

    @Test
    fun `load planet`() = runTest {
        val result = loadPlanet("planet.txt")
        val planet = Planet(Size(5, 4), listOf(Obstacle(2, 0), Obstacle(0, 3)))
        expectThat(result).isEqualTo(planet)
    }

    @Test
    fun `load rover`() = runTest {
        val result = loadRover("rover.txt")
        val rover = Rover(Position(0, 0), N)
        expectThat(result).isEqualTo(rover)
    }

    @Test
    fun `load commands`() = runTest {
        val result = runConsole("RRF") {
            loadCommands()
        }
        val commands = listOf(TurnRight, TurnRight, MoveForward)
        expectThat(result).isEqualTo(commands)
    }

    @Test
    fun `go to opposite angle`() = runTest {
        val result = runCaptureOutput("RBBLBRF") {
            runApp("planet.txt", "rover.txt")
        }
        expectThat(result).isEqualTo("${Console.GREEN}[OK] 4:3:E${Console.RESET}")
    }

    @Test
    fun `hit obstacle during commands execution`() = runTest {
        val result = runCaptureOutput("RFF") {
            runApp("planet.txt", "rover.txt")
        }
        expectThat(result).isEqualTo("${Console.GREEN}[OK] O:1:0:E${Console.RESET}")
    }

    @Test
    fun `invalid planet data`() = runTest {
        val result = runCaptureOutput("RBBLBRF") {
            runApp("planet_invalid_data.txt", "rover.txt")
        }
        expectThat(result).isEqualTo("${Console.RED}[ERROR] Planet parsing: invalid size: ax4${Console.RESET}")
    }

    @Test
    fun `invalid planet content`() = runTest {
        val result = runCaptureOutput("RBBLBRF") {
            runApp("planet_invalid_content.txt", "rover.txt")
        }
        expectThat(result).isEqualTo("${Console.RED}[ERROR] Invalid file content: planet_invalid_content.txt${Console.RESET}")
    }

    private suspend fun <A> runConsole(commands: String, program: suspend () -> A): A {
        val originalIn = System.`in`
        val originalOut = System.out

        try {
            val input = ByteArrayInputStream("$commands\n".toByteArray())
            val output = ByteArrayOutputStream()

            System.setIn(input)
            System.setOut(PrintStream(output))

            return program.invoke()
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
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