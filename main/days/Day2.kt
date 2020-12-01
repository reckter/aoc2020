package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.replace
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import kotlin.streams.asStream

class Day2 : Day {
    override val day = 2

    override fun solvePart1() {
        loadInput()
            .first()
            .split(",")
            .toIntegers()
            .let { it.replace(1, 12).replace(2, 2) }
            .let { start ->
                start.runProgram()
            }
            .tape[0]
            .solution(1)
    }

    override fun solvePart2() {
        val memory = loadInput()
            .first()
            .split(",")
            .toIntegers()

        (0 until 100)
            .flatMap { x ->
                (0 until 100).map{ x to it}
            }
            .asSequence()
            .asStream()
            .parallel()
            .map {
                val input = memory.replace(1, it.first).replace(2, it.second)
                val (_, tape) = input.runProgram()
                it to tape[0]?.toLong()
            }
            .filter { it.second == 19690720L}
            .findAny()
            .get()
            .first
            .let { (noun, verb) ->
                (100 * noun + verb)
                    .solution(2)
            }
//        repeat(100) { noun ->
//            repeat(100) { verb ->
//                val input = memory.replace(1, noun).replace(2, verb)
//                val (_, tape) = input.runProgram()
//                if (tape[0]?.toLong() == 19690720L) {
//                    (100 * noun + verb)
//                        .solution(2)
//                    return
//                }
//            }
//        }
    }
}

fun main(args: Array<String>) = solve<Day2>()
