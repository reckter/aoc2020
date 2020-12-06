package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.splitAtEmptyLine
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAt

class Day6 : Day {
    override val day = 6

    override fun solvePart1() {
        loadInput(trim = false)
            .splitAtEmptyLine()
            .map { group ->
                group
                    .reduce { acc, cur -> acc + cur}
                    .toList()
                    .distinct()
            }
            .sumBy { it.count() }
            .solution(1)
    }

    override fun solvePart2() {
        loadInput(trim = false)
            .splitAtEmptyLine()
            .map { group ->
                group
                    .reduce { acc, cur -> acc.filter { it in cur }}
                    .toList()
                    .distinct()
            }
            .sumBy { it.count() }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day6>()
