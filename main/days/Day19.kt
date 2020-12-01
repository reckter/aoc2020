package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asStream

class Day19 : Day {
    override val day = 19

    val code = loadInput()
        .first()
        .toTape()

    val outputCache = mutableMapOf<Pair<Int, Int>, Int>()
    fun getOutput(x: Int, y: Int): Int {
        if (!outputCache.containsKey(x to y)) {
            val result = code.runProgram(ArrayDeque(listOf(x.toBigInteger(), y.toBigInteger())))
            val ret = result.output.first().toInt()
            outputCache[x to y] = ret
            return ret
        }
        return outputCache[x to y]
            ?: error("$x, $y not in cache: ${outputCache[x to y]}")
    }

    override fun solvePart1() {
        (0 until 50)
            .flatMap { x ->
                (0 until 50)
                    .map { y ->
                        (x to y)
                    }
            }
            .asSequence()
            .asStream()
            .parallel()
            .map { getOutput(it.first, it.second) }
            .filter { it == 1 }
            .count()
            .solution(1)
    }

    fun printMap() {
        val maxX = outputCache.keys
            .map { it.first }
            .max() ?: error("no max x")

        val maxY = outputCache.keys
            .map { it.second }
            .max() ?: error("no max x")

        println("$maxX $maxY")
        (0..maxY)
            .forEach { y ->
                (0..maxX)
                    .forEach { x ->
                        print(outputCache[x to y] ?: " ")
                    }
                println()
            }
    }

    enum class Status {
        toHigh,
        exact,
        toLow
    }

    fun bisect(start: Int, end: Int, predicate: (Int) -> Status): Int {
        var lower = min(start, end)
        var higher = max(start, end)

        while (lower + 1 < higher) {
            val middle = (lower + higher) / 2
            when (predicate(middle)) {
                Status.toHigh -> higher = middle
                Status.exact -> return middle
                Status.toLow -> lower = middle
            }
        }

        return lower
    }

    override fun solvePart2() {
        var maxX = outputCache.keys
            .map { it.first }
            .max() ?: error("no max x")

        var maxY = outputCache.keys
            .map { it.second }
            .max() ?: error("no max x")

        (0 until 49)
            .asSequence()
            .flatMap { offset ->
                sequenceOf(
                    maxX - offset to maxY,
                    maxX to maxY - offset
                )
            }
            .first { outputCache[it] == 1 }
            .let {
                maxX = it.first
                maxY = it.second
            }

        var (rowSize, columnSize) = findRowAndColumnSize(maxX, maxY)

        var found = false

        while (!found) {
            if (rowSize >= 100 && columnSize >= 100) {
                found = true
                continue
            }

            val xPlus = min(max(columnSize, 1), 100 - rowSize)
            val yPlus = min(max(rowSize, 1), 100 - columnSize)
            maxX += xPlus
            maxY += yPlus

            findRowAndColumnSize(maxX, maxY)
                .let {
                    rowSize = it.first
                    columnSize = it.second
                }
        }

        ((maxX - 99) * 10000 + (maxY - 99))
            .solution(2)
    }

    private fun findRowAndColumnSize(
        maxX: Int,
        maxY: Int
    ): Pair<Int, Int> {
        val rowSize =
            maxX - bisect(max(0, maxX - 101), maxX) {
                if (getOutput(it, maxY) == 1)
                    Status.toHigh
                else
                    Status.toLow
            }

        val columnSize =
            maxY - bisect(max(0, maxY - 101), maxY) {
                if (getOutput(maxX, it) == 1)
                    Status.toHigh
                else
                    Status.toLow
            }
        return rowSize to columnSize
    }
}

fun main(args: Array<String>) = solve<Day19>()
