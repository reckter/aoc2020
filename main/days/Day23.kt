package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.print
import me.reckter.aoc.rotateLeft
import me.reckter.aoc.rotateRight
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import java.lang.Integer.max

class Day23 : Day {
    override val day = 23

    fun List<Int>.oneRound(max: Int): List<Int> {
        val takenOutCups = this.take(3)
        val restOfTable = this.drop(3)

        val indexToInsertAfter = generateSequence(this.last() - 1) {
            if (it < 0) max else it - 1
        }
            .filter { it !in takenOutCups }
            .map { restOfTable.indexOf(it) }
            .filter { it != -1 }
            .first()

        val newTable = (restOfTable.take(indexToInsertAfter + 1) + takenOutCups + restOfTable.drop(
            indexToInsertAfter + 1
        ))
            .rotateLeft(1)

        return newTable
    }

    override fun solvePart1() {
        var currentState = loadInput()
            .first()
            .split("")
            .filter { it != "" }
            .map { it.trim() }
            .toIntegers()
            .rotateLeft(1)

        repeat(100) {
            currentState = currentState.oneRound(9)
        }

        val oneInBack = currentState.rotateLeft(currentState.indexOf(1) + 1)
        oneInBack
            .dropLast(1)
            .joinToString("")
            .solution(1)
    }

    data class Cup(val value: Int) {
        lateinit var next: Cup
        fun getNext(n: Int) = generateSequence(this.next) { it.next }
            .take(n)
            .toList()
    }

    val cache by lazy {
        (1..1_000_000)
            .map {
                it to Cup(it)
            }
            .runningReduce { acc, cup ->
                acc.second.next = cup.second
                cup
            }
            .toMap()
    }

    override fun solvePart2() {
//        var currentState = loadInput()
//        .first()
//        .split("")
//        .filter { it != "" }
//        .map { it.trim() }
//        .toIntegers()
//        .rotateLeft(1) + (10 .. 1_000_000)
//
//        repeat(10_000_000) {
//            currentState = currentState.oneRound(1_000_000)
//        }
//
//        val oneInBack = currentState.rotateLeft(currentState.indexOf(1) + 1)
//        oneInBack
//            .reduce { acc, i -> acc * i }
//            .solution(2)

        val startingPattern = loadInput()
            .first()
            .split("")
            .filter { it != "" }
            .map { it.trim() }
            .toIntegers()
            .mapNotNull { cache[it] }

        val max = 1_000_000

        cache[max]!!.next = startingPattern.first()
        startingPattern
            .runningReduce { acc, cup ->
                acc.next = cup
                cup
            }
        startingPattern.last().next = cache[10]!!

        var current = startingPattern.first()
        repeat(10_000_000) {
            val moveable = current.getNext(3)

            val moveTo = generateSequence(current.value - 1) {
                if (it <= 1) max else it - 1
            }
                .filter { it in 1..max }
                .filter { it !in moveable.map { it.value } }
                .first()
                .let { cache[it] ?: error(it) }

            current.next = moveable.last().next

            val afterMoveTo = moveTo.next
            moveTo.next = moveable.first()
            moveable.last().next = afterMoveTo

            current = current.next
        }

        cache[1]!!
            .getNext(2)
            .map { it.value.toLong() }
            .reduce { acc, i -> acc * i }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day23>()
