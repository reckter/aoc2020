package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day15 : Day {
    override val day = 15

    fun getStart(): State {
        val turns = loadInput()
            .first()
            .split(",")
            .toIntegers()
        val map =
            turns
                .mapIndexed { index, it -> it to mutableListOf(index + 1,-1) }
                .toMap()
                .toMutableMap()
        val currentTurn = turns.size
        val lastNumber = turns.last()
        return State(
            map,
            currentTurn,
            lastNumber
        )
    }

    data class State(
        val lastPositions: MutableMap<Int, MutableList<Int>>,
        val currentTurn: Int,
        val lastNumber: Int
    )

    fun Pair<Int, Int>.next(lastPositions: MutableMap<Int, MutableList<Int>>): Pair<Int, Int> {
        val (currentTurn, lastNumber) = this
        val turn = currentTurn + 1
        val lastSpokenList = lastPositions[lastNumber] ?: error("this cant happen")
        if (lastSpokenList[1] > -1) {
            val nextNumber = currentTurn - lastSpokenList[1]
            val saved = lastPositions.getOrPut(nextNumber) { mutableListOf(-1,-1)}
            saved[1] = saved[0]
            saved[0] = turn
            return turn to nextNumber
        }

        val nextNumberSpokenAt = lastPositions[0]?.getOrNull(0) ?: -1
        lastPositions[0] = mutableListOf(turn,nextNumberSpokenAt)
        return turn to 0
    }

    override fun solvePart1() {
        val (lastPositions, turn, lastNumber) = getStart()
        generateSequence(turn to lastNumber) { current ->
            current.next(lastPositions)

        }
            .find { it.first == 2020 }
            ?.second
            ?.solution(1)
    }

    override fun solvePart2() {
        val (lastPositions, turn, lastNumber) = getStart()
        generateSequence(turn to lastNumber) { current ->
            current.next(lastPositions)
        }
            .find { it.first == 30000000 }
            ?.second
            ?.solution(2)
    }
}

fun main(args: Array<String>) = solve<Day15>()
