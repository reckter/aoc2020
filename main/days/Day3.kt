package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.math.BigDecimal

class Day3 : Day {
    override val day = 3

    data class Point(
        val x: Int,
        val y: Int,
        val tree: Boolean
    )

    val map by lazy {
        loadInput()
            .mapIndexed { x, line ->
                line
                    .mapIndexed { y, char ->
                        (x to y) to Point(
                            x,
                            y,
                            char == '#'
                        )
                    }
            }
            .flatten()
            .toMap()
    }
    val width by lazy {
        loadInput()
            .first()
            .length
    }

    fun treesInSlope(direction: Pair<Int, Int>): Int {
        var point = 0 to 0
        var trees = 0
        while (point.first <= map.count()) {
            if (map[point]?.tree == true) {
                trees++
            }
            point = (point.first + direction.first) to (point.second + direction.second)
            point = point.first to (point.second % width)
        }
        return trees
    }

    override fun solvePart1() {
        treesInSlope(1 to 3).solution(1)
    }

    override fun solvePart2() {
        listOf(
            1 to 1,
            1 to 3,
            1 to 5,
            1 to 7,
            2 to 1
        )
            .map { treesInSlope(it) }
            .map{it.toLong()}
            .reduce(Long::times)
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day3>()
