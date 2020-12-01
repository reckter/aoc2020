package me.reckter.aoc.days

import com.google.common.math.LongMath.pow
import me.reckter.aoc.Day
import me.reckter.aoc.neightbours
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day24 : Day {
    override val day = 24

    enum class Tile {
        Empty,
        Bug
    }

    val map by lazy {
        loadInput()
            .mapIndexed { x, list -> x to list }
            .flatMap { (x, list) ->
                list.split("").filter { it != "" }.mapIndexed { y, it -> (x to y) to it }
            }
            .map { (pos, it) ->
                pos to when (it) {
                    "." -> Tile.Empty
                    "#" -> Tile.Bug
                    else -> error("invalid tile found!")
                }
            }
            .toMap()
    }

    fun Map<Pair<Int, Int>, Tile>.bioDiversity(): Long {
        return this.filter { it.value == Tile.Bug }
            .map { (pos) ->
                pos.first * 5 + pos.second
            }
            .map { pow(2, it) }
            .sum()
    }

    fun Map<Pair<Int, Int>, Tile>.printMap() {
        val maxX = this.keys.map { it.first }.max() ?: error("no max x")
        val maxY = this.keys.map { it.second }.max() ?: error("no max y")

        println()
        (0..maxX).forEach { x ->
            (0..maxY).forEach { y ->
                val char = when (this[x to y]) {
                    Tile.Bug -> '#'
                    Tile.Empty -> '.'
                    else -> ' '
                }

                print(char)
            }
            println()
        }
    }

    override fun solvePart1() {

        var current = map
        var seen = mutableSetOf<Long>()
        while (current.bioDiversity() !in seen) {
            seen.add(current.bioDiversity())
            current.printMap()
            current = current
                .mapValues { (pos, it) ->
                    val neighbours = pos.neightbours()
                        .count { current[it] == Tile.Bug }

                    when (it) {
                        Tile.Bug -> when (neighbours) {
                            1 -> Tile.Bug
                            else -> Tile.Empty
                        }
                        Tile.Empty -> when (neighbours) {
                            1, 2 -> Tile.Bug
                            else -> Tile.Empty
                        }
                    }
                }
        }

        current.printMap()
        current.bioDiversity().solution(1)
    }

    fun Pair<Int, Int>.adjustNeigbour(
        currentDimension: Int,
        currentPos: Pair<Int, Int>
    ): List<Pair<Int, Pair<Int, Int>>> {
        return when {
            this == 2 to 2 -> {
                when (currentPos) {
                    1 to 2 -> listOf(
                        currentDimension + 1 to (0 to 0),
                        currentDimension + 1 to (0 to 1),
                        currentDimension + 1 to (0 to 2),
                        currentDimension + 1 to (0 to 3),
                        currentDimension + 1 to (0 to 4)
                    )
                    2 to 1 -> listOf(
                        currentDimension + 1 to (0 to 0),
                        currentDimension + 1 to (1 to 0),
                        currentDimension + 1 to (2 to 0),
                        currentDimension + 1 to (3 to 0),
                        currentDimension + 1 to (4 to 0)
                    )
                    3 to 2 -> listOf(
                        currentDimension + 1 to (4 to 0),
                        currentDimension + 1 to (4 to 1),
                        currentDimension + 1 to (4 to 2),
                        currentDimension + 1 to (4 to 3),
                        currentDimension + 1 to (4 to 4)
                    )
                    2 to 3 -> listOf(
                        currentDimension + 1 to (0 to 4),
                        currentDimension + 1 to (1 to 4),
                        currentDimension + 1 to (2 to 4),
                        currentDimension + 1 to (3 to 4),
                        currentDimension + 1 to (4 to 4)
                    )
                    else -> error("this can not be")
                }
            }
            this.first == -1 -> {
                listOf(currentDimension - 1 to (1 to 2))
            }
            this.first == 5 -> {
                listOf(currentDimension - 1 to (3 to 2))
            }
            this.second == -1 -> {
                listOf(currentDimension - 1 to (2 to 1))
            }
            this.second == 5 -> {
                listOf(currentDimension - 1 to (2 to 3))
            }
            else -> listOf(currentDimension to this)
        }
    }

    fun emptyBugMap(): Map<Pair<Int, Int>, Tile> {
        return (0 until 5)
            .flatMap { x ->
                (0 until 5)
                    .map { y -> (x to y) to Tile.Empty }
            }
            .toMap() - (2 to 2)
    }

    override fun solvePart2() {
        var current = mapOf(
            0 to (map - (2 to 2))
        )

        repeat(200) {
            val minDimension = (current.keys.min() ?: 0) - 1
            val maxDimension = (current.keys.max() ?: 0) + 1
            current = (minDimension..maxDimension)
                .map { dimension ->
                    val dimMap = current.getOrDefault(dimension, emptyBugMap())
                        .mapValues { (pos, it) ->
                            val neighbours = pos.neightbours()
                                .flatMap { neigbour -> neigbour.adjustNeigbour(dimension, pos) }
                                .count { (dimension, pos) ->
                                    current.getOrDefault(dimension, emptyMap())[pos] == Tile.Bug
                                }

                            when (it) {
                                Tile.Bug -> when (neighbours) {
                                    1 -> Tile.Bug
                                    else -> Tile.Empty
                                }
                                Tile.Empty -> when (neighbours) {
                                    1, 2 -> Tile.Bug
                                    else -> Tile.Empty
                                }
                            }

                        }

                    dimension to dimMap
                }
                .filter { (_, board) ->
                    board.any { it.value != Tile.Empty }
                }
                .toMap()
        }

        current
            .map {
                it.value.count { it.value == Tile.Bug }
            }
            .sum()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day24>()
