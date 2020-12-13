package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.lcm
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day13 : Day {
    override val day = 13

    val startTime by lazy {
        loadInput()
            .first()
            .toInt()
    }

    val busses by lazy {
        loadInput()
            .drop(1)
            .first()
            .split(",")
            .filter { it != "x"}
            .toIntegers()
    }

    override fun solvePart1() {
        generateSequence(startTime) {it + 1}
            .map { time ->
                time to busses.find { time % it == 0 }
            }
            .first { it.second != null }
            .let { (it.first - startTime) * (it.second ?: error("no null possible"))}
            .solution(1)
    }

    val itinerary by lazy {
        loadInput()
            .drop(1)
            .first()
            .split(",")
            .mapIndexed { index, it -> index to it}
            .filter { it.second != "x"}
            .map { it.first to it.second.toInt() }
            .map { it.first.toLong() to it.second.toLong() }

    }


    override fun solvePart2() {
        itinerary
            .fold(0L to 1L) { (start, step), (offset, bus) ->
                val next = generateSequence(start) { it + step}
                    .first { (it + offset) % bus  == 0L }

                val nextStep = lcm(step, bus)

                next to nextStep
            }
            .first
            .solution(2)

    }
}

fun main(args: Array<String>) = solve<Day13>()
