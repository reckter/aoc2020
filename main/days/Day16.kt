package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.repeatToSequence
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import kotlin.math.absoluteValue

class Day16 : Day {
    override val day = 16

    val patterns = mutableMapOf<Pair<Int, Int>, List<Int>>()
    fun generatePattern(position: Int, length: Int): List<Int> {
        if (!patterns.containsKey(position to length)) {
            val basePattern = listOf(0, 1, 0, -1).map { it }
            patterns[position to length] = generateSequence {
                basePattern
            }
                .flatten()
                .flatMap { pattern ->
                    (0..position).map { pattern }.asSequence()
                }
                .drop(1)
                .take(length)
                .toList()
        }
        return patterns[position to length] ?: error("no pattern found for $position")
    }

    fun generateFactors(length: Int, steps: Int): List<Int> {
        val initial = (0 until length)
            .map { 1 }

        return (0 until steps - 1)
            .fold(initial) { factors, _ ->
                var last = 0
                factors
                    .map {  it ->
                        last += it
                        last %= 10
                        last
                    }
            }
    }

    fun List<Int>.calculateElement(position: Int, offset: Int = 0): Int {
        val realSize = this.size + offset

        if ((position + 1 + offset) * 3 - 1 >= realSize) {
            return this.drop(position)
                .take(position + 1 + offset)
                .sum()
                .absoluteValue % 10
        }

        return this
            .drop(position)
            .chunked(position + 1 + offset)
            .mapIndexed { index, list ->
                when (index % 4) {
                    0 -> list.sum()
                    1 -> 0
                    2 -> -list.sum()
                    3 -> 0
                    else -> error("whuat")
                }
            }
            .sum()
            .absoluteValue % 10
    }

    override fun solvePart1() {
        val initial = loadInput()
            .first()
            .split("")
            .filter { it.isNotEmpty() }
            .toIntegers()


        (0 until 100)
            .fold(initial) { list, round ->
                list
                    .mapIndexed { index, _ -> list.calculateElement(index) }
//                    .print("at round $round")
            }
            .take(8)
            .joinToString("") { it.toString() }
            .solution(1)
    }

    override fun solvePart2() {
        val initial = loadInput()
            .first()
            .split("")
            .filter { it.isNotEmpty() }
            .toIntegers()
            .repeatToSequence(10000)
            .flatten()
            .toList()


        val offset = initial.take(7).joinToString("").toInt()

        val interestingPart = initial.drop(offset)

         (0 until 100)
            .fold(interestingPart.reversed()) { list, _ ->
                var last = 0
                list
                    .map {  it ->
                        last += it
                        last %= 10
                        last
                    }
            }
             .reversed()
             .take(8)
             .joinToString("")
             .solution(2)

        // my initial implementation based on pascals triangle
//        val factors = generateFactors(interestingPart.size, 100)
//        interestingPart
//            .take(8)
//            .mapIndexed { index, it ->
//                interestingPart.drop(index)
//                    .zip(factors.dropLast(index))
//                    .map { (a, b) -> a * b }
//                    .sum() % 10
//
//            }
//            .joinToString("") { it.toString() }
//            .solution(2)


    }
}

fun main() = solve<Day16>()
