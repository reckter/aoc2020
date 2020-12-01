package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.manhattenDistance
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day3 : Day {
    override val day = 3

    val map: Map<Pair<Int, Int>, Map<Int, Int>> by lazy {
        loadInput()
            .mapIndexed { index, list ->
                list.split(",")
                    .fold(listOf<Triple<Int, Int, Int>>(Triple(0, 0, 0))) { path, instruction ->
                        val (x, y, steps) = path.last()
                        val toAdd = (0..instruction.drop(1).toInt()).map {
                            when (instruction.first()) {
                                'R' -> Triple(x, y + it, steps + it)
                                'L' -> Triple(x, y - it, steps + it)
                                'U' -> Triple(x + it, y, steps + it)
                                'D' -> Triple(x - it, y, steps + it)
                                else -> error("unsupported direction! $instruction")
                            }
                        }

                        path + toAdd
                    }
                    .map { it to index }
            }
            .flatten()
            .groupBy { it.first.first to it.first.second }
            .mapValues { (_, value) ->
                value
                    .map { it.second to it.first.third }
                    .groupBy { it.first }
                    .mapValues { it.value.map { it.second }.min() ?: Int.MAX_VALUE }
            }
    }

    override fun solvePart1() {
        map
            .entries
            .filter { (_, lines) -> lines.size > 1 }
            .filter { (key, _) -> key != 0 to 0 }
            .map { (key, _) -> key.manhattenDistance() }
            .min()
            .solution(1)
    }

    override fun solvePart2() {
        map
            .entries
            .filter { (_, lines) -> lines.size > 1 }
            .filter { (key, _) -> key != 0 to 0 }
            .map { (_, value) -> value.values.sum() }
            .min()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day3>()
