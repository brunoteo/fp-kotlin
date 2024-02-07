package io.doubleloop

import io.doubleloop.version4.runApp
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
            println("[APP] Hello, world!")
            runApp("planet.txt", "rover.txt")
            println("[APP] Done")
    }
}