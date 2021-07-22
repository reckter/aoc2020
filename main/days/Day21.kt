package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day21 : Day {
    override val day = 21

    data class Line(
        val ingredients: List<String>,
        val allergens: List<String>
    )

    val lines by lazy {
        loadInput()
            .parseWithRegex("((?:\\w+ )*)\\(contains ((?:\\w+(?:, )?)*)\\)")
            .map { (ingredients, allergens) ->
                Line(
                    ingredients.split(" ").filterNot { it.isEmpty() },
                    allergens.split(",").map { it.trim() }
                )
            }
    }

    val ingredientsToLine by lazy {
        lines
            .flatMap { line -> line.ingredients.map { it to line } }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }
    }

    val allIngredients by lazy {
        lines
            .flatMap { line -> line.ingredients }
            .distinct()
    }

    val allergensToPossibleIngredients by lazy {
        val tmp = lines
            .flatMap { line -> line.allergens.map { it to line.ingredients } }
            .groupBy { it.first }
            .mapValues {
                it.value.map { it.second.toSet() }.reduce { acc, list -> acc.intersect(list) }
                    .toMutableSet()
            }
            .toMutableMap()

        val solvedAllergens = mutableSetOf<String>()

        var somethingChange = true

        while (somethingChange) {
            somethingChange = false
            tmp
                .filter { it.value.size == 1 }
                .filterKeys { it !in solvedAllergens }
                .forEach { (allergen, ingredients) ->
                    val ingredient = ingredients.single()
                    solvedAllergens.add(allergen)
                    somethingChange = true
                    tmp
                        .filterKeys { it != allergen }
                        .forEach { it.value.remove(ingredient) }
                }
        }
        tmp
    }

    val solvedIngredients by lazy {
        allergensToPossibleIngredients
            .map { it.value.single() to it.key }
            .toMap()
    }

    override fun solvePart1() {
        allIngredients.filterNot { solvedIngredients.containsKey(it) }
            .map { ingredientsToLine[it]?.size ?: error("no size found!") }
            .sum()
            .solution(1)
    }

    override fun solvePart2() {
        solvedIngredients
            .map { it }
            .sortedBy { it.value }
            .joinToString(",") { it.key }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day21>()
