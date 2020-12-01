package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day8 : Day {
    override val day = 8

    val image = run {
        loadInput()
            .first()
            .map { it.toString() }
            .toIntegers()
            .chunked(25 * 6)
            .map {
                it.chunked(25) // so we have [layer][y][x]
            }
    }

    override fun solvePart1() {
        val layer = image
            .minBy { it.flatten().count { it == 0 } }
            ?.flatten() ?: error("could not find a layer!")

        val ones = layer.count {it == 1}
        val twos = layer.count {it == 2}

        (ones * twos).solution(1)

    }

    override fun solvePart2() {
        val display = image
            .mapIndexed { index, list -> index to list }
            .flatMap { (layer, pixels) ->
                pixels
                    .mapIndexed { index, list -> index to list }
                    .flatMap { (y, row) ->
                        row.mapIndexed { x, value -> (layer to (x to y)) to value }
                    }
            }
            .groupBy { it.first.second}
            .mapValues { (_, values) ->
                values
                    .sortedBy { it.first.first }
                    .firstOrNull { it.second != 2 }
                    ?.second ?: 2
            }

        (0..5).joinToString("\n") { y ->
            (0..24).joinToString("") { x ->
                when(display[x to y]) {
                    2 -> "."
                    1 -> "X"
                    0 -> " "
                    else -> error("invalid pixel code ${display[x to y]} at ${x to y}")
                }
            }
        }
            .let { "\n$it" }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day8>()
