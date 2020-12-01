package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.compiler.Compiler
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import java.util.ArrayDeque

class Day1 : Day {
    override val day = 1


    override fun solvePart1() {
        val code = """
            |val size = input()
            |val ret = 0
            |while(size > 0) {
            |   size = size - 1
            |   ret = ret + input() / 3 - 2
            |}
            |output(ret)
        """.trimMargin()

        val intCode = Compiler.compile(code)

        val input = loadInput()
            .toIntegers()

        val realInput = listOf(input.size) + input
        val output = intCode
            .runProgram(ArrayDeque(realInput.map { it.toBigInteger() }) )

        output.output.single().toInt().solution(1)
//        loadInput()
//            .toIntegers()
//            .map { it.toFloat() }
//            .map { floor(it / 3) - 2 }
//            .map { it.toInt() }
//            .sum()
//            .solution(1)
    }

    override fun solvePart2() {
        val code = """
            |val size = input()
            |val ret = 0
            |while(size > 0) {
            |   size = size - 1
            |   
            |   val in = input()
            |   in = in / 3 - 2
            |   while(in > 0) {
            |       ret = ret + in
            |       in = in / 3 - 2
            |   }
            |}
            |output(ret)
        """.trimMargin()

        val intCode = Compiler.compile(code)

        val input = loadInput()
            .toIntegers()

        val realInput = listOf(input.size) + input
        val output = intCode
            .runProgram(ArrayDeque(realInput.map { it.toBigInteger() }))

        output.output.single().toInt().solution(1)
//        val fuelForParts = loadInput()
//            .toIntegers()
//            .map { it.toFloat() }
//            .map { floor(it / 3) - 2 }
//            .map { it.toInt() }
//            .map { part ->
//                generateSequence(part) {
//                    val fuelToUse = (floor(it.toFloat() / 3) - 2).toInt()
//                    if (fuelToUse <= 0)
//                        null
//                    else
//                        fuelToUse
//                }
//                    .sum()
//            }
//            .sum()
//            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day1>()
