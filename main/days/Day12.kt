package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.manhattenDistance
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day12 : Day {
    override val day = 12

    data class Instruction(
        val rotateRightBySteps: Int? = null,
        val forward: Int? = null,
        val inDirection: Pair<Direction, Int>? = null
    )

    enum class Direction {
        north,
        south,
        west,
        east
    }

    data class Position(
        val pos: Pair<Int, Int>,
        val direction: Direction
    )

    data class WaypointPosition(
        val ship: Pair<Int, Int>,
        val waypoint: Pair<Int, Int>
    )

    val instructions by lazy {
        loadInput()
            .map {
                when (it.first()) {
                    'N' -> Instruction(inDirection = Direction.north to it.drop(1).toInt())
                    'S' -> Instruction(inDirection = Direction.south to it.drop(1).toInt())
                    'W' -> Instruction(inDirection = Direction.west to it.drop(1).toInt())
                    'E' -> Instruction(inDirection = Direction.east to it.drop(1).toInt())
                    'F' -> Instruction(forward = it.drop(1).toInt())
                    'R' -> Instruction(rotateRightBySteps = it.drop(1).toInt() / 90)
                    'L' -> Instruction(rotateRightBySteps = 4 - it.drop(1).toInt() / 90)
                    else -> error("Invalid instruction $it")
                }
            }
    }

    fun Direction.rotateRight(steps: Int = 1): Direction {
        if (steps == 0) return this
        return when (this) {
            Direction.north -> Direction.east.rotateRight(steps - 1)
            Direction.east -> Direction.south.rotateRight(steps - 1)
            Direction.south -> Direction.west.rotateRight(steps - 1)
            Direction.west -> Direction.north.rotateRight(steps - 1)
        }
    }

    operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> {
        return this.first + other.first to this.second + other.second
    }
    operator fun Pair<Int, Int>.times(scalar: Int): Pair<Int, Int> {
        return this.first * scalar to  this.second * scalar
    }

    fun Pair<Int, Int>.rotateRight(steps: Int = 1): Pair<Int, Int> {
        if (steps == 0) return this

        val rotated =  this.second to -this.first
        return rotated.rotateRight(steps - 1)
    }

    fun moveDirection(pos: Pair<Int, Int>, direction: Direction, steps: Int): Pair<Int, Int> {
        return when (direction) {
            Direction.east -> pos.first + steps to pos.second
            Direction.west -> pos.first - steps to pos.second
            Direction.north -> pos.first to pos.second + steps
            Direction.south -> pos.first to pos.second - steps
        }
    }

    override fun solvePart1() {
        instructions
            .fold(Position(0 to 0, Direction.east)) { cur, instruction ->
                val movement = instruction.inDirection
                    ?: instruction.forward?.let { it -> cur.direction to it }

                val newPos = movement?.let { moveDirection(cur.pos, it.first, it.second) }
                    ?: cur.pos

                val newDirection =
                    instruction.rotateRightBySteps?.let { cur.direction.rotateRight(it) }
                        ?: cur.direction

                Position(newPos, newDirection)
            }
            .pos
            .manhattenDistance(0 to 0)
            .solution(1)
    }

    override fun solvePart2() {
        instructions
            .fold(WaypointPosition(0 to 0, 10 to 1)) { (ship, waypoint), instruction ->
                val newShip = instruction.forward?.let { ship + (waypoint * it) } ?: ship

                val movedWaypoint = instruction.inDirection?.let { moveDirection(waypoint, it.first, it.second) } ?: waypoint

                val rotatedWaypoint = instruction.rotateRightBySteps?.let { movedWaypoint.rotateRight(it) } ?: movedWaypoint

                WaypointPosition(newShip, rotatedWaypoint)
            }
            .ship
            .manhattenDistance(0 to 0)
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day12>()
