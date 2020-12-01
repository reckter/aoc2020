package me.reckter.aoc.days

import kotlinx.coroutines.runBlocking
import me.reckter.aoc.Day
import me.reckter.aoc.allCombinations
import me.reckter.aoc.intcode.runAsciComputerWithPreInput
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.print
import me.reckter.aoc.solve
import me.reckter.aoc.splitAt
import java.util.ArrayDeque

class Day25 : Day {
    override val day = 25

    val tape by lazy {
        loadInput()
            .first()
            .toTape()
    }

    fun getOutput(commands: List<String>): List<String> {
        val output = runBlocking {
            tape.runAsciComputerWithPreInput(commands, true)
        }
        return output.splitAt { it == 10.toBigInteger() }

            .map { it.map { it.toInt().toChar() }.joinToString("") }
    }

    fun getLastOutput(commands: List<String>): List<String> {
        val lines = getOutput(commands)
        return extractLastRoom(lines)
    }

    private fun extractLastRoom(lines: List<String>): List<String> {
        return lines.zipWithNext()
            .takeLastWhile {
                it.first != it.second || it.first != ""
            }
            .map { it.first }
            .dropWhile { it == "" }
    }

    data class Room(
        val name: String,
        val doors: List<String>,
        val items: List<String>
    )

    var map = mutableMapOf<Pair<Room, String>, Room>()

    var rooms = mutableMapOf<String, Room>()

    var pathToRooms = mutableMapOf<List<String>, Room>()

    fun Pair<Int, Int>.step(direction: String): Pair<Int, Int> {
        return when (direction) {
            "north" -> this.copy(first = this.first + 1)
            "south" -> this.copy(first = this.first - 1)
            "east" -> this.copy(second = this.second + 1)
            "west" -> this.copy(second = this.second - 1)
            else -> error("wrong direction: $direction")
        }
    }

    fun getUnexploredRoom(start: Room): List<String>? {
        val queue = ArrayDeque<Pair<Room, List<String>>>()
        queue.add(start to emptyList())

        val seen = mutableSetOf<Room>()
        while (queue.isNotEmpty()) {
            val (room, path) = queue.remove()

            seen.add(room)

            val neighbors = room
                .doors
                .map {
                    map[room to it] to it
                }
                .filter { (room) -> room !in seen }
                .map { (room, it) ->
                    room to path + it
                }

            val goal = neighbors
                .find { it.first == null }

            if (goal != null) {
                return goal.second
            }

            queue.addAll(
                neighbors
                    .filter { it.first != null }
                    .mapNotNull {
                        @Suppress("UNCHECKED_CAST")
                        it as? Pair<Room, List<String>>
                    }
            )
        }
        return null
    }

    fun getDirectionToRoom(from: Room, to: Room): List<String>? {
        val queue = ArrayDeque<Pair<Room, List<String>>>()
        queue.add(from to emptyList())

        val seen = mutableSetOf<Room>()
        while (queue.isNotEmpty()) {
            val (room, path) = queue.remove()

            seen.add(room)

            val neighbors = room
                .doors
                .map {
                    map[room to it] to path + it
                }
                .filter { (room) -> room !in seen }

            val goal = neighbors.find { it.first == to }
            if (goal != null) {
                return goal.second
            }

            queue.addAll(
                neighbors
                    .filter { it.first != null }
                    .mapNotNull {
                        @Suppress("UNCHECKED_CAST")
                        it as? Pair<Room, List<String>>
                    }
            )
        }
        return null
    }

    fun parseToRoom(output: List<String>): Room {
        val name = "== (.*) ==".toRegex().matchEntire(output.first())?.groups?.get(1)?.value
            ?: error("no name found out of output:\n$output")
        val doors = output
            .dropWhile { it != "Doors here lead:" }
            .drop(1)
            .takeWhile { it != "" }
            .map { it.removePrefix("- ") }

        val items =
            output
                .dropWhile { it != "Items here:" }
                .drop(1)
                .takeWhile { it != "" }
                .map { it.removePrefix("- ") }

        return Room(
            name = name,
            doors = doors,
            items = items
        )
    }

    fun exploreRoom(path: List<String>): Room {
        val output = getLastOutput(path)
        return parseToRoom(output)
    }

    fun exploreRooms(start: Room) {
        while (true) {
            val path = getUnexploredRoom(start)
                ?: break

            val room = exploreRoom(path)
            pathToRooms[path] = room
            room.print("next room")

            val parentRoom = pathToRooms[path.dropLast(1)]
                ?: continue

            map[parentRoom to path.last()] = room
        }
    }

    fun getItems(): Map<String, Room> {
        return map
            .entries
            .flatMap { (pos, room) ->
                room
                    .items
                    .map { it to pos.first }
            }
            .toMap()
            .filter {
                it.key !in listOf(
                    "photons",
                    "infinite loop",
                    "escape pod",
                    "molten lava",
                    "giant electromagnet"
                )
            }
    }

    override fun solvePart1() {
        val root = exploreRoom(getOutput(emptyList()))
        pathToRooms[emptyList()] = root
        rooms[root.name] = root

        exploreRooms(root)
        val itemMap = getItems()

        val itemCombinations = itemMap.keys
            .toList()
            .allCombinations()

        val testRoom = map.entries
            .find { it.value.name == "Security Checkpoint" }
            ?.key
            ?.first
            ?: error("no test room found")

        for (itemCombination in itemCombinations) {
            val (pos, path) = itemCombination
                .map { itemMap[it]!! to it }
                .fold(root to emptyList<String>()) { (pos, path), (room, item) ->
                    val pickup = "take $item"
                    val newDirections = getDirectionToRoom(pos, room)!!

                    room to path + newDirections + pickup
                }

            val test = getDirectionToRoom(pos, testRoom)
                ?: error("no directions to test room!")

            val output = getOutput(path + test + "west")
            itemCombination.print("trying combination")

            val lastRoom = extractLastRoom(output)
            output
                .find { it.startsWith("A loud") }
                .print("error")

            if (lastRoom.first() != "== Security Checkpoint ==") {
                output.joinToString("\n") { "out: $it" }.print("output")
            }
        }


        map.print("map")
    }

    override fun solvePart2() {
    }
}

fun main(args: Array<String>) = solve<Day25>()
