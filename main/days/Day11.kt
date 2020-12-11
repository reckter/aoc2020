package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day11 : Day {
    override val day = 11

    enum class Type {
        floor,
        empty,
        taken
    }

    fun countTakenSeatsNear(map: List<List<Type>>, x: Int, y: Int): Int {
        return (-1..1).sumBy { xOffset ->
            (-1..1).sumBy inner@{ yOffset ->
                if (xOffset == 0 && yOffset == 0)
                    return@inner 0
                val neighborX = x + xOffset
                if (neighborX < 0 || neighborX >= map.first().size)
                    return@inner 0
                val neighborY = y + yOffset
                if (neighborY < 0 || neighborY >= map.size)
                    return@inner 0

                if (map[neighborY][neighborX] == Type.taken)
                    1
                else
                    0
            }
        }
    }

    fun countTakenSeatsInSight(map: List<List<Type>>, x: Int, y: Int): Int {
        return (-1..1).sumBy { xOffset ->
            (-1..1).sumBy inner@{ yOffset ->
                if (xOffset == 0 && yOffset == 0)
                    return@inner 0

                var step = 1

                while (true) {
                    val neighborX = x + xOffset * step
                    if (neighborX < 0 || neighborX >= map.first().size)
                        return@inner 0
                    val neighborY = y + yOffset * step
                    if (neighborY < 0 || neighborY >= map.size)
                        return@inner 0
                    if (map[neighborY][neighborX] == Type.taken)
                        return@inner 1
                    if (map[neighborY][neighborX] == Type.empty)
                        return@inner 0

                    step++
                }
                @Suppress("UNREACHABLE_CODE")
                error("lol")
            }
        }
    }

    fun nextStep(
        map: List<List<Type>>,
        countNeighbors: (map: List<List<Type>>, x: Int, y: Int) -> Int,
        spawnAt: Int,
        dieAt: Int
    ): List<List<Type>> {
        return map.mapIndexed { y, row ->
            row.mapIndexed { x, spot ->
                when (spot) {
                    Type.floor -> spot
                    Type.empty ->
                        if (countNeighbors(map, x, y) == spawnAt)
                            Type.taken
                        else
                            Type.empty
                    Type.taken ->
                        if (countNeighbors(map, x, y) >= dieAt)
                            Type.empty
                        else
                            Type.taken
                }
            }
        }
    }

    fun List<List<Type>>.countTakenSeats(): Int {
        return this.sumBy { row -> row.count { it == Type.taken } }
    }

    val initial by lazy {
        loadInput()
            .map { row ->
                row.map { it.toString() }
                    .map {
                        when (it) {
                            "." -> Type.floor
                            "L" -> Type.empty
                            "#" -> Type.taken
                            else -> error("illegal floor tile: $it")
                        }
                    }
            }

    }

    override fun solvePart1() {
        generateSequence(initial) { cur ->
            val next = nextStep(cur, this::countTakenSeatsNear, 0, 4)
            if (next.countTakenSeats() != cur.countTakenSeats())
                next
            else
                null
        }
            .last()
            .countTakenSeats()
            .solution(1)
    }

    override fun solvePart2() {
        generateSequence(initial) { cur ->
            val next = nextStep(cur, this::countTakenSeatsInSight, 0, 5)
            if (next.countTakenSeats() != cur.countTakenSeats())
                next
            else
                null
        }
            .last()
            .countTakenSeats()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day11>()
