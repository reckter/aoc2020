package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.neightbours
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.util.ArrayDeque
import java.util.Comparator
import java.util.PriorityQueue

class Day20 : Day {
    override val day = 20

    sealed class Tile() {
        object Empty : Tile()
        object Wall : Tile()
        object Start : Tile()
        object Goal : Tile()
        data class Teleporter(
            val name: String,
            val between: List<Pair<Int, Int>>,
            val inner: Boolean
        ) : Tile()

        data class Marker(val marker: String) : Tile()
    }

    val map = run {
        val firstPass = loadInput()
            .mapIndexed { x, row ->
                row.mapIndexed { y, it -> (x to y) to it }
            }
            .flatten()
            .filter { it.second != ' ' }
            .toMap()
            .mapValues { (_, value) ->
                when (value) {
                    '#' -> Tile.Wall
                    '.' -> Tile.Empty
                    else -> Tile.Marker(value.toString())
                }
            }

        val maxX = firstPass.keys.map { it.first }.max() ?: error("no max x")
        val maxY = firstPass.keys.map { it.second }.max() ?: error("no max y")

        fun isOuterPortal(pos: Pair<Int, Int>): Boolean {
            val (x, y) = pos
            if (x - 3 < 0 || x + 3 > maxX)
                return true

            if (y - 3 < 0 || y + 3 > maxY)
                return true

            return false
        }

        val newEntries = firstPass
            .entries
            .filter { it.value is Tile.Marker }
            .map { (pos, marker) ->
                val otherMarker = pos
                    .neightbours()
                    .find { firstPass[it] is Tile.Marker }
                    ?: error("no other marker part found for $pos $marker")

                val openSpot = (pos.neightbours() + otherMarker.neightbours())
                    .find { firstPass[it] == Tile.Empty }
                    ?: error("no open spot found for marker $pos $marker (other: $otherMarker)")

                val name =
                    (
                            ((marker as? Tile.Marker)?.marker ?: error("not an marker tile!")) +
                                    ((firstPass[otherMarker] as? Tile.Marker)?.marker
                                        ?: error("other not a marker tile!"))
                            )
                        .toList()
                        .sorted()
                        .joinToString("")

                openSpot to Tile.Marker(name) to listOf(
                    pos to Tile.Wall,
                    otherMarker to Tile.Wall
                )
            }
            .distinctBy { it.first }
            .groupBy { it.first.second.marker }
            .mapValues { (name, entries) ->
                val between = entries.map { it.first.first }

                assert(between.size <= 2) {
                    "can not have portals between more than 2 locations! $between $name"
                }
                entries.flatMap { (marker, other) ->
                    between.map { it to Tile.Teleporter(name, between, !isOuterPortal(it)) } + other
                }
            }
            .values
            .flatten()
            .map {
                when ((it.second as? Tile.Teleporter)?.name) {
                    "AA" -> it.copy(second = Tile.Start)
                    "ZZ" -> it.copy(second = Tile.Goal)
                    else -> it
                }
            }

        newEntries.fold(firstPass) { cur, acc ->
            cur + acc
        }
    }

    fun dijkstra(
        map: Map<Pair<Int, Int>, Tile>,
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        followPortals: Boolean = true
    ): Int? {
        data class GraphNode(
            val parent: GraphNode? = null,
            val position: Pair<Int, Int>,
            val steps: Int
        )

        val queue = ArrayDeque<GraphNode>()
        queue.add(
            GraphNode(
                position = from,
                steps = 0
            )
        )

        val seen = mutableSetOf<Pair<Int, Int>>()

        while (queue.isNotEmpty()) {
            val next = queue.remove()
            seen.add(next.position)

            val neighbors = next.position
                .neightbours()
                .asSequence()
                .filter { map[it] != Tile.Wall }
                .filter {
                    val tile = map[it]
                    tile !is Tile.Wall
                }
                .map { pos ->
                    val teleporter = map[pos] as? Tile.Teleporter
                    if (followPortals && teleporter is Tile.Teleporter)
                        GraphNode(
                            parent = next,
                            position = teleporter.between.find { it != pos }
                                ?: error("could not find correct teleport destination! $teleporter $pos"),
                            steps = next.steps + 2
                        )
                    else
                        GraphNode(
                            parent = next,
                            position = pos,
                            steps = next.steps + 1
                        )
                }
                .filter { it.position !in seen }
                .toList()

            val goal = neighbors
                .find { it.position == to }

            if (goal != null) {
                return goal.steps
            }

            queue.addAll(neighbors)
        }

        return null
    }

    data class Portal(
        val name: String,
        val inner: Boolean
    )

    fun dijkstra(map: Map<Portal, Pair<Portal, List<Pair<Portal, Int>>>>, start: Portal): Int? {
        data class Node(
            val position: Portal,
            val steps: Int,
            val floor: Int
        )

        val queue = PriorityQueue<Node>(
            Comparator.comparingInt<Node> { it.steps }
        )
        queue.add(
            Node(
                position = start,
                steps = 0,
                floor = 0
            )
        )

        val seen = mutableSetOf<Pair<Portal, Int>>()

        while (queue.isNotEmpty()) {
            val next = queue.remove()
            seen.add(next.position to next.floor)

            val neighbors = map[next.position]
                ?.second
                .orEmpty()
                .asSequence()
                .map { (portal, steps) ->
                    Node(
                        position = map[portal]?.first ?: error("no output found for $portal"),
                        steps = next.steps + steps + if(portal.name == "ZZ") 0 else 1,
                        floor = next.floor +
                                when {
                                    portal.name == "ZZ" -> 0
                                    portal.inner -> 1
                                    else -> -1
                                }
                    )
                }
                .filter { it.position.name != "AA" }
                .filter { it.floor >= 0 }
                .filter { it.position to it.floor !in seen }
                .toList()

            val goal = neighbors
                .find { it.position.name == "ZZ" && it.floor == 0 }

            if (goal != null) {
                return goal.steps
            }

            queue.addAll(
                neighbors
                    .filter { it.position.name != "ZZ" }
            )
        }

        return null
    }

    override fun solvePart1() {
        val start = map.entries
            .find { it.value is Tile.Start }
            ?: error("could not find start!")

        val end = map.entries
            .find { it.value is Tile.Goal }
            ?: error("could not find goal!")

        dijkstra(map, start.key, end.key)
            .solution(1)
    }

    override fun solvePart2() {
        val portals = map
            .entries
            .filter { it.value is Tile.Teleporter || it.value is Tile.Goal || it.value is Tile.Start }
            .map {
                it.key to when (val tile = it.value) {
                    is Tile.Teleporter -> Portal(name = tile.name, inner = tile.inner)
                    is Tile.Goal -> Portal("ZZ", false)
                    is Tile.Start -> Portal("AA", false)
                    else -> error("unfilterted tile $it")
                }
            }

        val graph = portals
            .map { (from, portal) ->
                val output = portals
                    .map { it.second }
                    .find {
                        it.name == portal.name &&
                                (
                                        it.inner != portal.inner ||
                                                portal.name in listOf("AA", "ZZ")
                                        )
                    }
                    ?: error("could not find output portal")

                portal to
                        (output to portals
                            .filter { it.second != portal }
                            .mapNotNull { (to, toPortal) ->
                                dijkstra(
                                    map = map,
                                    from = from,
                                    to = to,
                                    followPortals = false
                                )
                                    ?.let {
                                        toPortal to it
                                    }
                            })
            }
            .toMap()

        val start = portals
            .map { it.second }
            .find { it.name == "AA" }
            ?: error("could not find start portal")

        dijkstra(graph, start)
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day20>()
