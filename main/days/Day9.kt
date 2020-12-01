package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.channelOf
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day9 : Day {
    override val day = 9

    override fun solvePart1() {
        val result = loadInput()
            .first()
            .split(",")
            .toIntegers()
            .runProgram(channelOf(1))

        result.output
            .first()
            .solution(1)
    }

    override fun solvePart2() {
        val result = loadInput()
            .first()
            .split(",")
            .toIntegers()
            .runProgram(channelOf(2))

        result.output
            .first()
            .solution(2)

    }
}

fun main(args: Array<String>) = solve<Day9>()
