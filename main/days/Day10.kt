package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.replace
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class Day10 : Day {
    override val day = 10

    val map = run {
        loadInput()
            .mapIndexed { y, row -> y to row }
            .flatMap { (y, row) ->
                row
                    .mapIndexed { x, thing -> x to thing }
                    .map { (x, thing) -> (x to y) to thing }
            }
            .filter { (_, thing) -> thing == '#' }
            .toMap()
    }

    fun Pair<Int, Int>.vectorTo(to: Pair<Int, Int>): Pair<Int, Int> =
        to.first - this.first to to.second - this.second

    fun Pair<Int, Int>.scaleDown(scale: Int) = this.first / scale to this.second / scale
    fun Pair<Int, Int>.multiply(scale: Int) = this.first * scale to this.second * scale

    fun Pair<Int, Int>.plus(add: Pair<Int, Int>) =
        this.first + add.first to this.second + add.second

    fun Pair<Int, Int>.length() =
        sqrt((this.first * this.first + this.second * this.second).toDouble())

    fun Pair<Int, Int>.dotProduct(to: Pair<Int, Int>) =
        this.first * to.first + this.second * to.second

    fun Pair<Int, Int>.angleTo(to: Pair<Int, Int>): Double {
        val value = acos(this.dotProduct(to) / (this.length() * to.length()))
        val direction = this.vectorTo(to)
        if(direction.first < 0)
            return 2 * PI - value
        return value
    }

    fun Pair<Int, Int>.angle() = (0 to -1).angleTo(this)

    fun gcd(a: Int, b: Int): Int {
        if (b == 0) return a

        return gcd(b, a % b)
    }

    val station by lazy {
        map
            .map { (position, _) ->
                position to map.keys
                    .filter { it != position }
                    .filter { checkPos ->
                        val vector = position.vectorTo(checkPos)
                        val factor = abs(gcd(vector.first, vector.second))

                        if (factor == 1) return@filter true

                        val direction = vector.scaleDown(factor)
                        (1 until factor)
                            .map { scale ->
                                direction.multiply(scale).plus(position)
                            }
                            .none { possibleBlocking ->
                                map.containsKey(possibleBlocking)
                            }
                    }
                    .count()
            }
            .maxBy { it.second }
            ?: error("no station found")
    }

    override fun solvePart1() {
        station.second
            .solution(1)
    }

    override fun solvePart2() {
        val coords = station.first

        val angles = map.keys
            .groupBy {
                val vector = coords.vectorTo(it)
                val factor = abs(gcd(vector.first, vector.second))

                if (factor == 0)
                    vector
                else
                    vector.scaleDown(factor)
            }
            .map { it.key.angle() to it.value}

        val sorted = angles.sortedBy { it.first }

        (1 until 200)
            .fold(Triple(angles, 0.0, 0 to 0)) { (list, angle, _), _ ->
                val sorted = list.sortedBy { it.first }
                val selected = sorted.firstOrNull { it.first > angle }
                    ?: sorted.first()
                val newAngle = selected.first

                val toRemove = selected.second.minBy { it.vectorTo(coords).length() }
                    ?: error("nothing found to remove!")

                if(selected.second.size == 1)
                    return@fold Triple(sorted - selected, newAngle, toRemove)

                val newList = sorted.replace(list.indexOf(selected), selected.first to selected.second - toRemove)

                Triple(newList, newAngle, toRemove)

            }
            .third
            .let { it.first * 100 + it.second }
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day10>()
