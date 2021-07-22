package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.fastExp
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import me.reckter.aoc.toLongs
import kotlin.streams.asStream

class Day25 : Day {
    override val day = 25

    override fun solvePart1() {
        val (doorPub, keyPub) = loadInput()
            .toLongs()

//        7 ^ doorPriv % 20201227 = doorPub

        val keyPriv = generateSequence(1) { it + 1}
            .map {
                it to fastExp(7, it.toLong(), 20201227)
//                it to (1 .. it).fold(1L) {acc, cur -> fastExp(7L, a(acc * 7) % 20201227}
            }
            .first { it.second == keyPub }
            .first
            .toLong()

        val key = fastExp(doorPub, keyPriv, 20201227)

        key.solution(1)


    }

    override fun solvePart2() {

    }
}

fun main(args: Array<String>) = solve<Day25>()
