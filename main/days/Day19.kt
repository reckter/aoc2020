package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day19 : Day {
    override val day = 19

    val input by lazy { loadInput(trim = false) }
    val rulesStrings by lazy {
        input
            .takeWhile { it != "" }
            .parseWithRegex("(\\d*): (.*)")
            .map { (ruleName, rule) -> ruleName.toInt() to rule }
            .toMap()
    }

    fun expandRule(rule: String, rulesStrings: Map<Int, String>, maxDepth: Int = 1000): String {
        if (maxDepth == 0) return ""
        if (rule.contains("\"")) return rule.replace("\"", "")
        val orParts = rule.split("|")
        val ret = orParts
            .joinToString("|") { or ->
                "(${
                    or.split(" ")
                        .filter { it != "" }
                        .joinToString("") {
                            "(${
                                expandRule(
                                    rulesStrings[it.toInt()] ?: error("invalid rule $it"),
                                    rulesStrings,
                                    maxDepth - 1
                                )
                            })"
                        }
                })"
            }
        return ret
    }

    val regex by lazy {
        expandRule(rulesStrings[0] ?: error("rule 0 does not exist"), rulesStrings)
            .toRegex()
    }

    val rules by lazy {
        val fixedStrings = rulesStrings + listOf(
            8 to "42 | 42 8",
            11 to "42 31 | 42 11 31"
        )
        fixedStrings
            .mapValues { (_, rule) ->
                if (rule.contains("\"")) {
                    Rule(direct = rule.replace("\"", ""))
                } else {
                    Rule(
                        groups = rule
                            .split("|")
                            .map { it.trim().split(" ").toIntegers() }
                    )
                }
            }
    }

    data class Rule(
        val direct: String? = null,
        val groups: List<List<Int>>? = null
    ) {
        fun toString(rules: Map<Int, Rule>): String {
            if (this.direct != null) return direct

            return groups?.joinToString("|") {
                val rule = it.joinToString("") { it ->
                    if (it == 8 || it == 11) it.toString()
                    else rules[it]?.toString(rules) ?: "-"
                }

                "($rule)"
            } ?: " "
        }
    }

    override fun solvePart1() {
        input
            .dropWhile { it != "" }
            .drop(1)
            .count { it.matches(regex) }
            .solution(1)
    }

    fun String.match(rule: Rule, intendation: Int = 0): Pair<Boolean, String?> {
        println("| ".repeat(intendation) + "match $this with ${rule.toString()}")
        if (rule.direct != null) {
            if (!this.startsWith(rule.direct)) return (false to null).print("| ".repeat(intendation) + "return")
            val left = this.removePrefix(rule.direct)
            return (true to left).print("| ".repeat(intendation) + "return")
        }
        if (rule.groups == null) error("groups or direct must be given")

        val ret = rule.groups
            .map { group ->
                group.fold<Int, Pair<Boolean, String?>>(true to this) { cur: Pair<Boolean, String?>, rule ->
                    if (!cur.first) return false to cur.second
                    cur.second?.match(
                        rules[rule] ?: error("found invalid rule $rule"),
                        intendation + 1
                    )
                        ?: error("so second!")
                }
            }
            .find { it.first }
            ?: false to this

        return ret
            .print("| ".repeat(intendation) + "return")
    }

    override fun solvePart2() {

        val regex = expandRule(
            rulesStrings[0] ?: error("rule 0 does not exist"),
            rulesStrings + listOf(
                8 to "42 | 42 8",
                11 to "42 31 | 42 11 31"
            ),
            200
        )
            .toRegex()

        input
            .dropWhile { it != "" }
            .drop(1)
            .count { it.matches(regex) }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day19>()
