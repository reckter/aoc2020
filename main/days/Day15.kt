package me.reckter.aoc.days

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.startProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.math.BigInteger
import java.util.ArrayDeque

class Day15 : Day {
    override val day = 15

    val tape = run {
        loadInput()
            .first()
            .toTape()
    }

    enum class Tile {
        wall,
        empty,
        oxygenTank
    }

    val map = mutableMapOf<Pair<Int, Int>, Tile>()

    fun Pair<Int, Int>.neightbours(): List<Pair<Int, Int>> {
        return listOf(
            this.copy(first = this.first - 1),
            this.copy(second = this.second - 1),
            this.copy(first = this.first + 1),
            this.copy(second = this.second + 1)
        )
    }

    data class Context(
        val output: Channel<BigInteger>,
        val input: Channel<BigInteger>
    )

    fun dijkstra(
        position: Pair<Int, Int>,
        predicate: (Pair<Int, Int>, Tile?) -> Boolean
    ): List<Pair<Int, Int>>? {
        val queue = ArrayDeque<Pair<Pair<Int, Int>, Pair<Int, Int>?>>()
        queue.offer(position to null)

        val seen = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()

        fun calculatePath(found: Pair<Int, Int>): List<Pair<Int, Int>> {
            return generateSequence(found) { current ->
                seen[current]
            }
                .toList()
                .reversed()
        }

        while (queue.isNotEmpty()) {
            val (next, parent) = queue.pop()
            seen[next] = parent

            val neighbours = next.neightbours()
                .filter { !seen.containsKey(it) }
                .filter { item -> queue.none { it.first == item } }
                .filter { map[it] != Tile.wall }
                .shuffled()

            val goal = neighbours
                .find { predicate(it, map[it]) }

            if (goal != null) {
                seen[goal] = next
                return calculatePath(goal)
            }


            neighbours.forEach { queue.offer(it to next) }
        }
        return null
    }

    var robotPosition: Pair<Int, Int> = 0 to 0

    fun drawMap() {
//        val minX = map.keys
//            .map { it.first }
//            .min() ?: 0
//        val maxX = map.keys
//            .map { it.first }
//            .max() ?: 1
//        val minY = map.keys
//            .map { it.second }
//            .min() ?: 0
//        val maxY = map.keys
//            .map { it.second }
//            .max() ?: 1
        val minY = -30
        val maxY = 30
        val minX = -30
        val maxX = 30

        val string = (minY..maxY).joinToString("\n") { y ->
            (minX..maxX).joinToString("") { x ->
                if (x to y == robotPosition)
                    "R"
                else
                    when (map[x to y]) {
                        Tile.wall -> "█"
                        Tile.empty -> "."
                        Tile.oxygenTank -> "O"
                        else -> " "
                    }
            }
        }

        println("\nmap:")
        println(string)
    }

    enum class Direction(val command: Int) {
        north(1),
        south(2),
        west(3),
        east(4);

        companion object {
            fun findNeighbourDirection(from: Pair<Int, Int>, neighbor: Pair<Int, Int>): Direction {
                val xDif = from.first - neighbor.first
                val yDif = from.second - neighbor.second

                return when {
                    xDif == 1 -> north
                    xDif == -1 -> south
                    yDif == 1 -> east
                    yDif == -1 -> west
                    else -> error("invalid x and y diff: $xDif $yDif\n$from $neighbor")
                }
            }
        }
    }

    fun directionToGoIn(path: List<Pair<Int, Int>>): Pair<Int, Pair<Int, Int>>? {
        val pathToGo = path.dropWhile { it != robotPosition }
            .drop(1)

        if (pathToGo.isEmpty()) return null

        val nextPosition = pathToGo.first()
        val direction = Direction.findNeighbourDirection(robotPosition, nextPosition)

        return direction.command to nextPosition
    }

    suspend fun Context.followPath(path: List<Pair<Int, Int>>): Boolean {
        while (true) {
            val (nextDirection, nextPosition) = directionToGoIn(path) ?: return false

            input.send(nextDirection.toBigInteger())
            val result = output.receive().toInt()
            val tile = Tile.values()[result]

            map[nextPosition] = tile

            if (result != 0) {
                robotPosition = nextPosition
            }

            if (!me.reckter.aoc.Context.testMode)
                drawMap()

            if (result == 0) {
                return false
            }
        }
    }

    override fun solvePart1() {
        val input = Channel<BigInteger>()
        val runningResult = runBlocking { tape.startProgram(input) }

        val context = Context(
            input = input,
            output = runningResult.output
        )

        runBlocking {
            var fullyExplored = false
            while (!fullyExplored) {
                val path = dijkstra(robotPosition) { _, tile -> tile == null }
                if (path == null) {
                    fullyExplored = true
                    continue
                }
                context.followPath(path)
            }
        }
        dijkstra(0 to 0) { _, tile -> tile == Tile.oxygenTank }
            ?.size
            ?.let { it - 1 }
            .solution(1)
        // a* or something
    }

    fun flood(start: Pair<Int, Int>): Int {
        val queue = ArrayDeque<List<Pair<Int, Int>>>()
        queue.offer(listOf(start))

        val seen = mutableSetOf<Pair<Int, Int>>()

        var steps = 0
        while (queue.isNotEmpty()) {
            val next = queue.pop()

            seen.addAll(next)

            val wave = next
                .flatMap { it.neightbours()}
                .distinct()
                .filter { map[it] != Tile.wall }
                .filter { map[it] != null }
                .filter { it !in seen }

            if(wave.isEmpty()) break

            steps++
            queue.add(wave)
        }
        return steps
    }

    override fun solvePart2() {
        val (oxygenTankPosition) = map
            .entries
            .find { it.value == Tile.oxygenTank }
            ?: error("no oxygen Tank found")

        flood(oxygenTankPosition)
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day15>()
