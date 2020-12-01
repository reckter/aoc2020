package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allPairings
import me.reckter.aoc.permutations
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day1 : Day {
    override val day = 1

    override fun solvePart1() {
        loadInput()
            .toIntegers()
            .allPairings(includeSelf = false, bothDirections = false)
            .find { (a, b)-> a + b == 2020 }
            ?.let { (a, b) -> a * b }
            ?.solution(1)
    }

    override fun solvePart2() {
        val input = loadInput().toIntegers()

        input
            .allPairings(input)
            .allPairings(input)
            .map { (pair, last) ->
                Triple(pair.first, pair.second, last)
            }
            .find { (a, b , c) -> a + b + c == 2020 }
            ?.let { (a, b, c) -> a * b * c }
            ?.solution(2)
    }
}

fun main(args: Array<String>) = solve<Day1>()
