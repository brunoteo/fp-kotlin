package io.doubleloop.version5.domain

import io.doubleloop.version5.domain.Command
import io.doubleloop.version5.domain.ObstacleDetected
import io.doubleloop.version5.domain.Planet
import io.doubleloop.version5.domain.Rover

interface MissionSource {
    suspend fun readPlanet(): Planet
    suspend fun readRover(): Rover
}

interface CommandsChannel {
    suspend fun receive(): List<Command>
}

interface MissionReport {
    suspend fun sequenceCompleted(rover: Rover)
    suspend fun obstacleDetected(rover: ObstacleDetected)
    suspend fun error(error: Throwable)
}