package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.asRange
import me.reckter.aoc.compiler.Compiler
import me.reckter.aoc.digits
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import java.util.ArrayDeque

class Day4 : Day {
    override val day = 4

    override fun solvePart1() {
        loadInput()
            .first()
            .split("-")
            .toIntegers()
            .asRange()
            .filter { it.digits() == it.digits().sorted() }
            .filter { it.digits().groupBy { it }.values.any { it.size >= 2 }}
            .count()
            .solution(1)

        val code = """
            val length = input()
            val in: int[] = new int(length)
            val i = 0
            while(i < length) {
                in[i] = input() - 48
                i = i + 1
            }
            val minus: int = 0
            i = 0
            while(i < length) {
                if(in[i] == -3) { // a minus is 45, so -3 if we normalize to the 0 ascii code (48)
                    minus = i
                    i = length
                }
                i = i + 1
            }
            val min = parseNumber(in, 0, minus - 1)
            val max = parseNumber(in, minus + 1, length - 1)
            
            val result = 0
            while(min < max) {
                if (isSorted(min) & containsAGroupOf2OrMore(min)) {
                    result = result + 1
                }
                min = min + 1
            }
            output(result)
            
            fun isSorted(a: int): bool {
                val lastDigit = 10
                while(a > 0) {
                    val current = a % 10
                    if (current > lastDigit) {
                        return false
                    }
                    lastDigit = current
                    a = a / 10
                }
                return true
            }
            
            val arrayToCheck: int[] = new int(10)
            fun containsAGroupOf2OrMore(a: int): bool {
                val i = 0
                while(i < 10) {
                    arrayToCheck[i] = 0
                    i = i + 1
                }
                while(a > 0) {
                    val digit = a % 10
                    arrayToCheck[digit] = arrayToCheck[digit] + 1
                    a = a / 10
                }
                i = 0
                while(i < 10) {
                    if(arrayToCheck[i] > 1) {
                        return true
                    }
                    i = i + 1
                }
                return false
            }
            
            fun parseNumber(array: int[], from: int, to: int): int {
                val ret = 0
                while(from < to | from == to) {
                    ret = ret * 10 + array[from]
                    from = from + 1
                }
                return ret
            }
        """.trimIndent()

        val programm = Compiler.compile(code)

        val input = loadInput().first().map { it.toInt().toBigInteger() }
       // val output = programm.runProgram(ArrayDeque(listOf(input.size.toBigInteger()) + input), printOutput = true)
//        println(output.output.map { it.toInt() })

    }

    override fun solvePart2() {
        loadInput()
            .first()
            .split("-")
            .toIntegers()
            .asRange()
            .filter { it.digits() == it.digits().sorted() }
            .filter { it.digits().groupBy { it }.values.any { it.size == 2 }}
            .count()
            .solution(2)

        val code = """
            val length = input()
            val in: int[] = new int(length)
            val i = 0
            while(i < length) {
                in[i] = input() - 48
                i = i + 1
            }
            val minus: int = 0
            i = 0
            while(i < length) {
                if(in[i] == -3) { // a minus is 45, so -3 if we normalize to the 0 ascii code (48)
                    minus = i
                    i = length
                }
                i = i + 1
            }
            val min = parseNumber(in, 0, minus - 1)
            val max = parseNumber(in, minus + 1, length - 1)
            
            val result = 0
            while(min < max) {
                if (isSorted(min) & containsAGroupOf2OrMore(min)) {
                    result = result + 1
                }
                min = min + 1
            }
            output(result)
            
            fun isSorted(a: int): bool {
                val lastDigit = 10
                while(a > 0) {
                    val current = a % 10
                    if (current > lastDigit) {
                        return false
                    }
                    lastDigit = current
                    a = a / 10
                }
                return true
            }
            
            val arrayToCheck: int[] = new int(10)
            fun containsAGroupOf2OrMore(a: int): bool {
                val i = 0
                while(i < 10) {
                    arrayToCheck[i] = 0
                    i = i + 1
                }
                while(a > 0) {
                    val digit = a % 10
                    arrayToCheck[digit] = arrayToCheck[digit] + 1
                    a = a / 10
                }
                i = 0
                while(i < 10) {
                    if(arrayToCheck[i] == 2) {
                        return true
                    }
                    i = i + 1
                }
                return false
            }
            
            fun parseNumber(array: int[], from: int, to: int): int {
                val ret = 0
                while(from < to | from == to) {
                    ret = ret * 10 + array[from]
                    from = from + 1
                }
                return ret
            }
        """.trimIndent()

        val programm = Compiler.compile(code, extendedInstructionSet = false)

        val input = loadInput().first().map { it.toInt().toBigInteger() }
        val output = programm.runProgram(ArrayDeque(listOf(input.size.toBigInteger()) + input), printOutput = true)
        println(output.output.map { it.toInt() })
    }
}

fun main(args: Array<String>) = solve<Day4>()
