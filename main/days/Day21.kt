package me.reckter.aoc.days

import kotlinx.coroutines.runBlocking
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.runAsciComputerWithPreInput
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day21 : Day {
    override val day = 21

    val tape = run {
        loadInput()
            .first()
            .toTape()
    }

    override fun solvePart1() {
        val program = """
            NOT A T
            OR T J
            NOT B T
            OR T J
            NOT C T 
            OR T J
            AND D J
            WALK
        """.trimIndent()
            .split("\n")
        runBlocking {
            tape.runAsciComputerWithPreInput(program)
                .last()
                .solution(1)
        }
    }

    override fun solvePart2() {
        val program = """
            NOT A J
            NOT B T
            OR T J
            NOT C T 
            OR T J
            AND D J
            NOT E T
            NOT T T
            OR H T
            AND T J
            RUN
        """.trimIndent()
            .split("\n")
        runBlocking {
            tape.runAsciComputerWithPreInput(program)
                .last()
                .solution(2)
        }
    }
}

fun main(args: Array<String>) = solve<Day21>()
