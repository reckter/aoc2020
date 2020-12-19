package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day16 : Day {
    override val day = 16

    val input by lazy { loadInput() }

    val rules by lazy {
        input
            .takeWhile { it !== "" }
            .parseWithRegex("(.*?): (.*)?")
            .map { (name, rules) ->
                name to rules.split("or")
                    .map { it.trim() }
                    .map {
                        it.split("-")
                            .toIntegers()
                    }
                    .map { (min, max) -> min to max }
            }
            .toMap()
    }

    val myTicket by lazy {
        input.dropWhile { it != "your ticket:" }
            .drop(1)
            .first()
            .split(",")
            .toIntegers()
    }

    val otherTickets by lazy {
        input.dropWhile { it != "nearby tickets:" }
            .drop(1)
            .map {
                it.split(",")
                    .toIntegers()
            }
    }

    fun couldBeField(rule: List<Pair<Int, Int>>, value: Int): Boolean {
        return rule
            .any { it.first <= value && it.second >= value }
    }

    override fun solvePart1() {
        otherTickets
            .flatMap { ticket ->
                ticket.filter { value ->
                    rules.none { couldBeField(it.value, value) }
                }
            }
            .sum()
            .solution(1)
    }

    fun getIndexForRule(rule: List<Pair<Int, Int>>, validTickets: List<List<Int>>): List<Int> {
        return validTickets.first()
            .indices
            .filter { index ->
                validTickets
                    .all { ticket ->
                        couldBeField(rule, ticket[index])
                    }
            }
    }

    override fun solvePart2() {
        val validTickets = otherTickets
            .filter { ticket ->
                ticket.all { value ->
                    rules.any { couldBeField(it.value, value) }
                }
            }

        val possibleFields = rules
            .map { it.key to getIndexForRule(it.value, validTickets) }
            .toMap()

        val ruleIndexes = generateSequence(possibleFields) { map ->
            val taken = map
                .values
                .filter { it.size == 1 }
                .flatten()
            if (taken.size == map.size) null
            else map
                .mapValues {
                    if (it.value.size == 1) it.value
                    else it.value - taken
                }
        }
            .last()

        ruleIndexes
            .filter { it.key.startsWith("departure")}
            .map { it.value.single()}
            .map { myTicket[it].toLong()}
            .reduce{a, b -> a * b}
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day16>()
