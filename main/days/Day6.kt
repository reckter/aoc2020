package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.commonPrefix
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.util.ArrayDeque

class Day6 : Day {
    override val day = 6

    val orbitedBy: Map<String, List<String>> = run {
        loadInput()
            .map { it.split(")") }
            .groupBy { (ref) -> ref}
            .mapValues { ( _, values) -> values.map { (_, satellite) -> satellite}}
    }

    val orbits: Map<String, String> = run {
        loadInput()
            .map { it.split(")") }
            .associateBy { (_, satellite) -> satellite}
            .mapValues { ( _, value) -> value[0]}
    }

    override fun solvePart1() {
        val queue = ArrayDeque(listOf("COM"))

        val orbitMap = mutableMapOf<String, Int>()
        orbitMap["COM"] = 0

        while (queue.size > 0) {
            val next = queue.pop()

            val orbits = orbitMap[next] ?: error("did encounter an element without path info")

            val neighbors = orbitedBy[next] ?: listOf()
            neighbors.forEach {
                orbitMap[it] = orbits + 1
                queue.add(it)
            }
        }

        orbitMap.values
            .sum()
            .solution(1)

    }

    fun pathTo(node: String): List<String> {
        return generateSequence(node) { cur ->
            orbits[cur]
        }
            .toList()
            .reversed()
    }

    override fun solvePart2() {

        val start = orbits["YOU"] ?: error("could not find start")
        val end = orbits["SAN"] ?: error("could not find end")

        val pathToStart = pathTo(start)
        val pathToEnd = pathTo(end)

        val commonHobs = pathToStart
            .commonPrefix(pathToEnd)
            .size

        (pathToStart.size + pathToEnd.size - commonHobs * 2).solution(2)
    }
}

fun main(args: Array<String>) = solve<Day6>()
