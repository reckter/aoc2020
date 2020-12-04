package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day2 : Day {
    override val day = 2

    data class Entry(
        val first: Int,
        val second: Int,
        val letter: Char,
        val password: String
    )

    val entries by lazy {
        loadInput()
            .parseWithRegex("(\\d+)-(\\d+) (.): (.*)$")
            .map { (minStr, maxStr, code, password) ->
                Entry(
                    minStr.toInt(),
                    maxStr.toInt(),
                    code.first(),
                    password
                )
            }
    }

    override fun solvePart1() {
        entries
            .count {
                val count = it.password.count { char -> char == it.letter }
                it.first <= count && count <= it.second
            }
            .solution(1)
    }

    override fun solvePart2() {
        entries
            .count {
                (it.password[it.first - 1] == it.letter) != (it.password[it.second - 1] == it.letter)
            }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day2>()
