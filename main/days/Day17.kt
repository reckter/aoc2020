package me.reckter.aoc.days

import kotlinx.coroutines.channels.Channel
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.neightbours
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.math.BigInteger
import java.util.ArrayDeque

@Suppress("USELESS_ELVIS")
class Day17 : Day {
    override val day = 17

    val tape = run {
        loadInput()
            .first()
            .toTape()
    }

    enum class Tile {
        space,
        scafholding,
        robot_north,
        robot_east,
        robot_south,
        robot_west,
        robot_space,
        new_line,
    }

    enum class Rotation(val step: (cur: Pair<Int, Int>) -> Pair<Int, Int>) {
        up({ it.copy(second = it.second - 1) }),
        right({ it.copy(first = it.first + 1) }),
        down({ it.copy(second = it.second + 1) }),
        left({ it.copy(first = it.first - 1) });

        fun rotateRight(): Rotation = values()[(this.ordinal + 1) % values().size]
        fun rotateLeft(): Rotation = values()[(this.ordinal - 1 + values().size) % values().size]
    }

    val map by lazy {
        val input = Channel<BigInteger>()

        val runningResult = tape.runProgram(input)

        runningResult.output
            .map { it.toInt() }
            .map {
                when (it) {
                    46 -> Tile.space
                    35 -> Tile.scafholding
                    94 -> Tile.robot_north
                    62 -> Tile.robot_east
                    60 -> Tile.robot_west
                    118 -> Tile.robot_south
                    88 -> Tile.robot_space
                    10 -> Tile.new_line

                    else -> error("unkown tile! ${it}")
                }
            }
            .splitAt { it -> it == Tile.new_line }
            .mapIndexed { index, it -> index to it.mapIndexed { index, it -> index to it } }
            .flatMap { (y, row) ->
                row.map { (x, it) ->
                    (x to y) to it
                }
            }
            .toMap()
    }

    private fun findIndicesWithoutOverlap(
        rest: List<String>,
        needle: List<String>
    ): List<Int> {
        val repetitions = rest
            .indicesOf(needle)

        return repetitions
            .filterIndexed { index, it ->
                if (index == 0) return@filterIndexed true

                repetitions[index - 1] <= it - needle.size
            }
    }

    fun List<String>.toAsciiInput(): List<Int> =
        this.joinToString(",")
            .map { it.toInt() }

    fun List<String>.findSplits(): List<List<String>> {
        val maxSize = 20
        val splits = mutableListOf(20, 20, 20)
        val functionNames = listOf("A", "B", "C")

        val functions = mutableMapOf<String, List<String>>()

        while (true) {
            val main = (0 until 3)
                .fold(this) { path, number ->
                    val repetition = path.dropWhile { it in functionNames }
                        .take(splits[number])

                    functions[functionNames[number]] = repetition

                    val indicesToReplace = findIndicesWithoutOverlap(path, repetition)

                    path
                        .splitAtIndexed { index, _ ->
                            index in indicesToReplace
                        }
                        .mapIndexed { index, it -> index to it }
                        .flatMap { (index, it) ->
                            if (index == 0)
                                it
                            else
                                listOf(functionNames[number]) + it.drop(repetition.size - 1)
                        }
                }

            if (main.all { it in functionNames } && functions.all { it.value.toAsciiInput().size <= 20 }) {
                return listOf(main) +
                        functionNames
                            .mapNotNull {
                                functions[it]
                            }
            }

            splits[splits.size - 1]--
            (2 downTo 1)
                .map {
                    if (splits[it] == 1) {
                        splits[it - 1]--
                        splits[it] = maxSize
                    }
                }

            if (splits.first() == 1) {
                error("no split found!")
            }
        }
    }

    override fun solvePart1() {
        map
            .filter { it.value == Tile.scafholding }
            .filter { (pos, _) ->
                pos.neightbours()
                    .all { map[it] == Tile.scafholding }
            }
            .map { (pos, _) -> pos.first * pos.second }
            .sum()
            .solution(1)
    }

    override fun solvePart2() {
        var (robotPosition, tile) = map
            .entries
            .find {
                it.value in listOf(
                    Tile.robot_north,
                    Tile.robot_east,
                    Tile.robot_south,
                    Tile.robot_west,
                    Tile.robot_space
                )
            }
            ?: error("could not find robot!")

        var rotation = when (tile) {
            Tile.robot_north -> Rotation.up
            Tile.robot_east -> Rotation.right
            Tile.robot_south -> Rotation.down
            Tile.robot_west -> Rotation.left

            else -> error("invalid robot starting position $tile")
        }

        var end = false

        val path = mutableListOf<String>()
        while (!end) {
            if (map[rotation.step(robotPosition)] == Tile.scafholding) {
                path[path.size - 1] = (path.last().toInt() + 1).toString()
                robotPosition = rotation.step(robotPosition)
                continue
            }

            if (map[rotation.rotateRight().step(robotPosition)] == Tile.scafholding) {
                rotation = rotation.rotateRight()
                path.add("R")
                path.add("1")
                robotPosition = rotation.step(robotPosition)
                continue
            }

            if (map[rotation.rotateLeft().step(robotPosition)] == Tile.scafholding) {
                rotation = rotation.rotateLeft()
                path.add("L")
                path.add("1")
                robotPosition = rotation.step(robotPosition)
                continue
            }

            end = true
        }

        val splits = path.findSplits()

        val program = tape + (0L to 2.toBigInteger())

        val input =
            splits
                .flatMap { it.toAsciiInput() + listOf(10) } +
                    listOf('n'.toInt(), 10)

        val result = program.runProgram(ArrayDeque(input.map { it.toBigInteger() }))

        result.output
            .last()
            .solution(2)
    }
}

private fun <E> List<E>.indicesOf(needle: List<E>): List<Int> {
    return (0..this.size - needle.size)
        .filter {
            this.drop(it)
                .zip(needle)
                .all { (a, b) -> a == b }
        }
}

private fun <T> Iterable<T>.splitAtIndexed(predicate: (Int, T) -> Boolean): Iterable<Iterable<T>> {
    return this.foldIndexed(mutableListOf(mutableListOf<T>())) { index, lists, element ->
        if (predicate(index, element)) {
            lists.add(mutableListOf())
        } else {
            lists.last().add(element)
        }
        lists
    }
}

private fun <T> Iterable<T>.splitAt(predicate: (T) -> Boolean): Iterable<Iterable<T>> {
    return this.fold(mutableListOf(mutableListOf<T>())) { lists, element ->
        if (predicate(element)) {
            lists.add(mutableListOf())
        } else {
            lists.last().add(element)
        }
        lists
    }
}

fun main(args: Array<String>) = solve<Day17>()
