package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.math.ceil

class Day14 : Day {
    override val day = 14

    data class Reaction(
        val product: Pair<Int, String>,
        val ingredients: List<Pair<Int, String>>
    )

    val reactions = run {
        loadInput()
            .parseWithRegex("(.*) => (.*) (.*)")
            .map { (ingredients, amount, product) ->
                val parsedIngredients = ingredients
                    .split(",")
                    .map { it.trim() }
                    .parseWithRegex("(.*) (.*)")
                    .map { (amount, ingredient) ->
                        amount.toInt() to ingredient
                    }

                Reaction(
                    product = amount.toInt() to product,
                    ingredients = parsedIngredients
                )
            }
            .associateBy { it.product.second }

    }

    fun produce(input: Map<String, Pair<Long, Long>>): Map<String, Pair<Long, Long>> {
        val produces = input.toMutableMap()
        while (true) {
            val (name, quantities) = produces.entries
                .filter { (key, _) -> key != "ORE" }
                .find { (key, value) -> value.second > value.first }
                ?: break

            val toProduce = quantities.second - quantities.first

            val reaction = reactions[name] ?: error("no reaction found for $name")
            val multiplier = ceil(toProduce.toDouble() / reaction.product.first.toDouble()).toLong()
            val producing = multiplier * reaction.product.first

            val alreadyProducing = produces.getOrDefault(name, 0L to 0L)
            produces[name] = alreadyProducing.copy(first = alreadyProducing.first + producing)

            reaction.ingredients
                .map { (amount, name) ->
                    amount * multiplier to name
                }
                .forEach { (amount, name) ->
                    val production = produces.getOrDefault(name, 0L to 0L)
                    produces[name] = production.copy(second = production.second + amount)
                }
        }
        return produces
    }

    override fun solvePart1() {

        val input = mutableMapOf(
            "FUEL" to (0L to 1L)
        )

        produce(input)["ORE"]
            ?.second
            .solution(1)
    }

    fun oneBiseptStep(
        stepSize: Long,
        input: Map<String, Pair<Long, Long>>
    ): Map<String, Pair<Long, Long>>? {
        val biseptInput = input +
                Pair("FUEL", input.getOrDefault("FUEL", 0L to 0L)
                    .let { it.copy(second = it.second + stepSize) }
                )
        val output = produce(biseptInput)
        if(output["ORE"]?.second ?: 0> 1000000000000L) {
            return null
        }

        return output
    }

    override fun solvePart2() {

        var step = 2L
        var products = mapOf(
            "FUEL" to (0L to 1L)
        )

        while (step > 0) {
            val newProducts = oneBiseptStep(step, products)

            step.print("stepSize")
            if (newProducts == null) {
                step /= 2
                continue
            }

            products = newProducts
            step *= 2
        }
        products["FUEL"]
            ?.second
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day14>()
