package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day5 : Day {
    override val day = 5



    fun getSeatId(pass: String): Int {
        val (_,row) = pass.take(7)
            .fold(0 to 127) { (min, max), curr ->
            val mid = (min + max) / 2
            if(curr == 'B') {
                mid to max
            } else {
                min to mid
            }
        }

        val (_,column) = pass.drop(7)
            .fold(0 to 7) { (min, max), curr ->
                val mid = (min + max) / 2
                if(curr == 'R') {
                    mid to max
                } else {
                    min to mid
                }
            }

        return row * 8 + column
    }
    override fun solvePart1() {
        loadInput()
            .map { getSeatId(it) }
            .max()
            .solution(1)

    }

    override fun solvePart2() {

    }
}

fun main(args: Array<String>) = solve<Day5>()
