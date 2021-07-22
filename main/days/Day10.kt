package me.reckter.aoc.days

import kotlinx.coroutines.yield
import me.reckter.aoc.Day
import me.reckter.aoc.allCombinations
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day10 : Day {
    override val day = 10

    override fun solvePart1() {
        loadInput()
            .toIntegers()
            .let { it + listOf((it.maxOrNull() ?: error("")) + 3, 0) }
            .sorted()
            .zipWithNext()
            .map { (a, b) -> b - a }
            .groupBy { it }
            .let { (it[1]?.size ?: error("")) * (it[3]?.size ?: error("")) }
            .solution(1)

    }

    override fun solvePart2() {
        val input = loadInput()
            .toIntegers()
            .sorted()
        val max = (input.maxOrNull() ?: error("no max!")) + 3

        val chain = (input + listOf(0, max)).sorted()

        chain
            .reversed()
            .fold(mapOf(max to 1L)) { cache, current ->
                val possibilities = cache
                    .entries
                    .filter { it.key - 3 <= current }
                    .map { it.value }
                    .sum()

                cache + (current to possibilities)
            }
            .get(0)
            .solution(2)


    }
}

fun main(args: Array<String>) = solve<Day10>()
