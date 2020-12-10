package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day7 : Day {
    override val day = 7

    data class RulePart(
        val color: String,
        val amount: Int
    )

    val rules by lazy {
        loadInput()
            .parseWithRegex("^(.*) bags contain (.*)$")
            .map { (color, rules) ->
                color to rules
                    .replace(".", "")
                    .split(",")
                    .map { it.trim() }
                    .parseWithRegex("(\\d+) (.*) bags?")
                    .map { (numberString, colorString) ->
                        RulePart(
                            colorString,
                            numberString.toInt()
                        )
                    }
            }
            .toMap()
    }

    fun findAllThatCanDirectlyFit(color: String): List<String> {
        return rules
            .filter { (_, rules) -> rules.any { it.color == color } }
            .map { it.key }
    }

    override fun solvePart1() {
        generateSequence(listOf("shiny gold")) { current ->
            val next =
                current
                    .flatMap { findAllThatCanDirectlyFit(it) }
                    .let { current + it }
                    .distinct()

            if (next == current)
                null
            else next
        }
            .last()
            .count()
            .let { it - 1 }
            .solution(1)
    }


    fun getTotalBagsInside(color: String): Int {
        return rules[color]
            ?.map { rules ->
                (getTotalBagsInside(rules.color) + 1)* rules.amount
            }
            ?.sum()
            ?: error("no rule found for $color")
    }

    override fun solvePart2() {
        getTotalBagsInside("shiny gold")
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day7>()
