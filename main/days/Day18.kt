package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.neightbours
import me.reckter.aoc.replace
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.util.ArrayDeque
import java.util.OptionalInt
import kotlin.math.min

class Day18 : Day {
    override val day = 18

    sealed class Tile {
        object Empty : Tile()
        object Wall : Tile()
        data class Key(val id: Char) : Tile()
        data class Door(val id: Char) : Tile()
    }

    val part = 0

    val startingPosition = run {
        loadInput(part)
            .mapIndexed { index, list -> index to list }
            .flatMap { (y, list) ->
                list
                    .mapIndexed { x, it ->
                        (x to y) to it
                    }
            }
            .find { it.second == '@' }
            ?.first
            ?: error("could not find starting possition!")
    }

    val map = run {
        loadInput(part)
            .mapIndexed { index, list -> index to list }
            .flatMap { (y, list) ->
                list
                    .mapIndexed { x, it ->
                        (x to y) to when {
                            it == '#' -> Tile.Wall
                            it == '.' || it == '@' -> Tile.Empty
                            it.isLowerCase() -> Tile.Key(it)
                            it.isUpperCase() -> Tile.Door(it.toLowerCase())
                            else -> error("invalid tile found $it at ($x;$y)")
                        }
                    }
            }
            .toMap()
    }

    val keyMap: Map<Char, Pair<Int, Int>> = run {
        map
            .entries
            .filter { it.value is Tile.Key }
            .map { (it.value as Tile.Key).id to it.key }
            .toMap()
    }

    val doorMap = run {
        map
            .entries
            .filter { it.value is Tile.Door }
            .map { (it.value as Tile.Door).id to it.key }
            .toMap()
    }

    val dijkstraCache = mutableMapOf<Pair<Int, Int>, List<Triple<Char, Int, List<Char>>>>()
    // only return steps needed, no actual path
    fun dijkstra(
        map: Map<Pair<Int, Int>, Tile>,
        start: Pair<Int, Int>
    ): List<Triple<Char, Int, List<Char>>> {
        val cacheKey = start
        if (dijkstraCache.containsKey(cacheKey)) {
            return dijkstraCache[cacheKey]!!
        }

        data class Node(
            val position: Pair<Int, Int>,
            val steps: Int,
            val passedDoors: List<Char>
        )

        val queue = ArrayDeque<Node>()
        queue.add(
            Node(
                position = start,
                steps = 0,
                passedDoors = emptyList()
            )
        )

        val seen = mutableSetOf<Pair<Int, Int>>()

        val ret = mutableListOf<Triple<Char, Int, List<Char>>>()
        while (queue.isNotEmpty()) {
            val next = queue.remove()
            seen.add(next.position)

            val neighbors = next.position
                .neightbours()
                .filter { map[it] != Tile.Wall }
                .filter {
                    val tile = map[it]
                    tile !is Tile.Wall
                }
                .filter { it !in seen }
                .map {
                    Node(
                        position = it,
                        steps = next.steps + 1,
                        passedDoors = next.passedDoors + listOfNotNull(map[it].takeIf { it is Tile.Door }).map { (it as Tile.Door).id }
                    )
                }

            neighbors
                .map { it to (map[it.position] ?: error("position not found")) }
                .filter { it.second is Tile.Key }
                .forEach { (it, tile) ->
                    val ley = (tile as Tile.Key).id
                    ret.add(
                        Triple(
                            ley,
                            it.steps,
                            it.passedDoors
                        )
                    )
                }

            queue.addAll(neighbors)
        }

        dijkstraCache[cacheKey] = ret

        return ret
    }

    val fastestPathCache = mutableMapOf<Pair<Set<Char>, List<Pair<Int, Int>>>, Int>()
    fun findFastestPath(
        map: Map<Pair<Int, Int>, Tile>,
        positions: List<Pair<Int, Int>>,
        keysToPickup: List<Char> = keyMap.keys.toList(),
        stepsSoFar: Int = 0,
        best: Int = Int.MAX_VALUE,
        depth: Int = 0
    ): Int? {
        val cacheKey = keysToPickup.toSet() to positions
        fastestPathCache[cacheKey]
            ?.let {
                return it + stepsSoFar
            }

        if (keysToPickup.isEmpty()) {
            return stepsSoFar
        }

        var bestSoFar = best

        val ret = positions
            .mapNotNull { robot ->
                dijkstra(map, robot)
                    .asSequence()
                    .filter { it.first in keysToPickup }
                    .filter { it.third.none { key -> key in keysToPickup } }
                    .sortedBy { it.second }
                    .mapNotNull { (key, steps) ->
                        val rest = findFastestPath(
                            map,
                            positions.replace(
                                positions.indexOf(robot),
                                keyMap[key] ?: error("key not found")
                            ),
                            keysToPickup - key,
                            stepsSoFar + steps,
                            bestSoFar,
                            depth + 1
                        )
                        rest
                            ?.let {
                                bestSoFar = min(bestSoFar, rest)
                            }
                        rest
                    }
                    .min()
            }
            .min()

        if (ret != null) {
            fastestPathCache[cacheKey] = ret - stepsSoFar
        }

        return ret
    }

    override fun solvePart1() {
        dijkstraCache.clear()
        fastestPathCache.clear()
        findFastestPath(map, listOf(startingPosition))
            .solution(1)
    }

    override fun solvePart2() {
        dijkstraCache.clear()
        fastestPathCache.clear()
        val part2Map = map.toMutableMap()
        val replacement = listOf(
            listOf(Tile.Empty, Tile.Wall, Tile.Empty),
            listOf(Tile.Wall, Tile.Wall, Tile.Wall),
            listOf(Tile.Empty, Tile.Wall, Tile.Empty)
        )

        replacement
            .forEachIndexed { y, list ->
                list.forEachIndexed { x, tile ->
                    part2Map[startingPosition.first + (x - 1) to startingPosition.second + (y - 1)] =
                        tile
                }
            }

        val newStartingPositions =
            listOf(-1, 1).flatMap { x ->
                listOf(-1, 1).map { y ->
                    startingPosition.first + x to startingPosition.second + y
                }
            }

        findFastestPath(part2Map, newStartingPositions)
            .solution(2)
    }
}

private fun OptionalInt.getOrNull(): Int? {
    if (!this.isPresent) return null
    return this.asInt
}

fun main(args: Array<String>) = solve<Day18>()
