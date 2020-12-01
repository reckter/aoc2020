package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.lang.Math.abs
import kotlin.math.sign

class Day12 : Day {
    override val day = 12

    data class Moon(
        val position: Triple<Int, Int, Int>,
        val velocity: Triple<Int, Int, Int>
    )

    fun Moon.attractionTo(other: Moon): Triple<Int, Int, Int> {
        val x = (other.position.first - this.position.first).sign
        val y = (other.position.second - this.position.second).sign
        val z = (other.position.third - this.position.third).sign

        return Triple(x, y, z)
    }

    fun Triple<Int, Int, Int>.energy() =
        abs(this.first) + abs(this.second) + abs(this.third)

    fun Moon.energy() =
        position.energy() * velocity.energy()

    operator fun Triple<Int, Int, Int>.plus(to: Triple<Int, Int, Int>): Triple<Int, Int, Int> =
        Triple(
            this.first + to.first,
            this.second + to.second,
            this.third + to.third
        )

    val scan = run {
        loadInput()
            .parseWithRegex("<x=(.*), y=(.*), z=(.*)>")
            .map { (xString, yString, zString) ->
                Triple(
                    xString.toInt(),
                    yString.toInt(),
                    zString.toInt()
                )
            }
    }

    fun Triple<Int, Int, Int>.hash() = "$first,$second,$third"

    fun Moon.hash() =
        listOf(
            position.first, position.second, position.third,
            velocity.first, velocity.second, velocity.third
        )

    fun Moon.slice(extractor: (Triple<Int, Int, Int>) -> Int): List<Int> = listOf(
        extractor(position),
        extractor(velocity)
    )

    fun List<Moon>.hash() = this.map { it.hash() }
    fun List<Moon>.slice(extractor: (Triple<Int, Int, Int>) -> Int) =
        this.map { it.slice(extractor) }

    val moons by lazy {
        scan
            .map {
                Moon(
                    position = it,
                    velocity = Triple(0, 0, 0)
                )
            }
    }

    override fun solvePart1() {
        (0 until 1000).fold(moons) { current, _ ->
            current
                .map { moon ->
                    moon.copy(
                        velocity = moon.velocity + current
                            .map { moon.attractionTo(it) }
                            .reduce { acc, triple -> acc + triple }
                    )
                }
                .map {
                    it.copy(
                        position = it.position + it.velocity
                    )
                }
        }
            .sumBy { it.energy() }
            .solution(1)
    }

    override fun solvePart2() {
        // map from <(X,y,z), slice> to step
        val pattern = mutableMapOf<Pair<Int, List<List<Int>>>, Int>()

        // map from (x,y,z) to <offset, repeat>
        val loops = mutableMapOf<Int, Pair<Int, Int>>()

        generateSequence(moons to 0) { (moons, steps) ->
            val next = moons
                .map { moon ->
                    moon.copy(
                        velocity = moon.velocity + moons
                            .map { moon.attractionTo(it) }
                            .reduce { acc, triple -> acc + triple }
                    )
                }
                .map {
                    it.copy(
                        position = it.position + it.velocity
                    )
                }

            val slices =
                listOf(
                    0 to next.slice { it.first },
                    1 to next.slice { it.second },
                    2 to next.slice { it.third }
                )

            slices.forEach { (id, slice) ->
                if (pattern.containsKey(id to slice)) {
                    val firstSeen = pattern[id to slice] ?: error("lol")
                    if (!loops.containsKey(id))
                        loops[id] = firstSeen to steps - firstSeen
                } else {
                    pattern[id to slice] = steps
                }
            }

            if (loops.size == 3)
                null
            else
                next to steps + 1

        }
            .map { it.first }
            .mapIndexed { index: Int, list: List<Moon> ->
                if (index % 1_000_000 == 0) index.print("current")
                list
            }
            .all { true }

        loops.print("loops")

        val lcm1 = lcm(
            loops[0]?.second?.toLong() ?: error("no 0 loop"),
            loops[1]?.second?.toLong() ?: error("no 1 loop")
        )

        val lcm2 = lcm(
            lcm1,
            loops[2]?.second?.toLong() ?: error(" no 2 loop")
        )

        lcm2.solution(2)

//
//        generateSequence(offset.toLong()) { cur -> cur + loop }
//            .drop(1)
//            .mapIndexed { index, it ->
//                if (index % 1_000_000 == 0) index.print("current")
//                it
//            }
//            .first { value ->
//                loops.values
//                    .all { (testOffset, testLoop) -> value % testLoop == testOffset.toLong() }
//            }
//            .solution(2)
    }

    fun lcm(n1: Long, n2: Long): Long {
        var gcd = 1L
        var i = 1L
        while (i <= n1 && i <= n2) {
            // Checks if i is factor of both integers
            if (n1 % i == 0L && n2 % i == 0L)
                gcd = i
            ++i
        }
        val lcm = n1 * n2 / gcd
        return lcm
    }
}

fun main(args: Array<String>) = solve<Day12>()
