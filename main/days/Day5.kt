package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.channelOf
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day5 : Day {
    override val day = 5

    override fun solvePart1() {
        val tape = loadInput()
            .first()
            .split(",")
            .toIntegers()

        val result = tape.runProgram(channelOf(1))
        result.output
            .last()
            .solution(1)
    }

    override fun solvePart2() {
        val tape = loadInput()
            .first()
            .split(",")
            .toIntegers()

        val result = tape.runProgram(channelOf(5))
        result.output
            .first()
            .solution(2)

    }
}

fun main(args: Array<String>) = solve<Day5>()
