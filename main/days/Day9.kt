package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allPairings
import me.reckter.aoc.runsOfLength
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toBigIntegers
import me.reckter.aoc.toIntegers
import me.reckter.aoc.toLongs

class Day9 : Day {
    override val day = 9

    val input by lazy {
        loadInput()
            .toLongs()

    }

    val goal by lazy {
        val preamble = 25
        generateSequence(input.take(preamble) to preamble.toLong()) { (list, index) ->
            if (list.isEmpty()) return@generateSequence null
            val next = input[index.toInt()]
            val validPair = list
                .allPairings(false)
                .find { (a, b) -> a + b == next }
            if (validPair == null) {
                emptyList<Long>() to next
            } else {
                (list.drop(1) + next) to index + 1
            }
        }
            .last()
            .second
    }

    override fun solvePart1() {
        goal
            .solution(1)
    }

    override fun solvePart2() {
        generateSequence(2 to emptyList<List<Long>>()) { (length) ->
            length + 1 to input.runsOfLength(length)
        }
            .flatMap { it.second.asSequence() }
            .find { it.sum() == goal }
            ?.let { it.sorted() }
            ?.let { it.first() + it.last() }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day9>()
