package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.cords.d3.Cord3D
import me.reckter.aoc.cords.d3.getNeighbors
import me.reckter.aoc.cords.d3.plus
import me.reckter.aoc.cords.d4.Cord4D
import me.reckter.aoc.cords.d4.getNeighbors
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.util.ArrayDeque

class Day17 : Day {
    override val day = 17

    val start3D by lazy {
        loadInput()
            .mapIndexed { y, row ->
                row.mapIndexed { x, it ->
                    Cord3D(x, y, 0) to (it == '#')
                }
            }
            .flatten()
            .toMap()
    }

    val start4D by lazy {
        start3D.mapKeys { (it, _) -> Cord4D(it.x, it.y, it.z, 0) }
    }

    fun Map<Cord3D<Int>, Boolean>.isActive(at: Cord3D<Int>): Boolean {
        return this[at] ?: false
    }

    fun Map<Cord4D<Int>, Boolean>.isActive(at: Cord4D<Int>): Boolean {
        return this[at] ?: false
    }

    fun step3D(current: Map<Cord3D<Int>, Boolean>): Map<Cord3D<Int>, Boolean> {
        val queue = ArrayDeque<Cord3D<Int>>(current.keys)

        val nextState = mutableMapOf<Cord3D<Int>, Boolean>()
        return step(
            nextState,
            queue,
            current,
            Cord3D<Int>::getNeighbors
        ) { this[it] ?: false }
    }

    fun <T> step(
        nextState: MutableMap<T, Boolean>,
        queue: ArrayDeque<T>,
        current: Map<T, Boolean>,
        getNeighbors: T.() -> List<T>,
        isActive: Map<T, Boolean>.(at: T) -> Boolean
    ): Map<T, Boolean> {
        val seen = mutableSetOf<T>()
        seen.addAll(queue)
        while (queue.isNotEmpty()) {
            val next = queue.pop()
            val neightbours = next.getNeighbors()
            val activeNeighbors = neightbours.count { current.isActive(it) }
            val isSelfActive = current.isActive(next)

            val activeAfter = when {
                isSelfActive && activeNeighbors >= 2 && activeNeighbors <= 3 -> true
                !isSelfActive && activeNeighbors == 3 -> true
                else -> false
            }

            if (activeAfter) {
                nextState[next] = activeAfter
            }

            if (isSelfActive) {
                neightbours
                    .filter { it !in seen }
                    .forEach { queue.add(it); seen.add(it) }
            }
        }
        return nextState
    }

    fun step4D(current: Map<Cord4D<Int>, Boolean>): Map<Cord4D<Int>, Boolean> {
        val queue = ArrayDeque<Cord4D<Int>>(current.keys)

        val nextState = mutableMapOf<Cord4D<Int>, Boolean>()

        return step(
            nextState,
            queue,
            current,
            Cord4D<Int>::getNeighbors
        ) { this[it] ?: false }
    }

    override fun solvePart1() {
        (1..6).fold(start3D) { cur, _ -> step3D(cur) }
            .count()
            .solution(1)
    }

    override fun solvePart2() {
        (1..6).fold(start4D) { cur, _ -> step4D(cur) }
            .count()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day17>()
