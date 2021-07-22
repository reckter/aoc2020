package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.cords.cube.Cube
import me.reckter.aoc.cords.cube.getNeighbors
import me.reckter.aoc.cords.cube.plus
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day24 : Day {
    override val day = 24


    val mapToDirection = listOf(
        "ne" to Cube.Direction.NorthEast,
        "e" to Cube.Direction.East,
        "se" to Cube.Direction.SouthEast,
        "sw" to Cube.Direction.SouthWest,
        "w" to Cube.Direction.West,
        "nw" to Cube.Direction.NorthWest,
	)

    fun mapStringToDirections(str: String): List<Cube.Direction> {
        if(str.isEmpty()) return emptyList()
        val (directionString, direction ) =mapToDirection.find { str.startsWith(it.first) }!!

        return listOf(direction) + mapStringToDirections(str.removePrefix(directionString))
    }

    val startingTiles by lazy {
        val tiles = mutableMapOf<Cube, Boolean>()

        loadInput()
            .map {
                mapStringToDirections(it)
            }
            .forEach {
                val tile = it.map {it.direction}
                    .fold(Cube(0,0,0)) { acc, dir -> acc.plus(dir)}

                tiles[tile] = !tiles.getOrDefault(tile, false)
            }
        tiles
    }
    override fun solvePart1() {
        startingTiles.count { it.value }
            .solution(1)

    }

    override fun solvePart2() {
        var tiles = startingTiles.toMap()

        repeat(100) {
            val tilesToWatch = tiles.keys
                .flatMap { it.getNeighbors() + it }
                .distinct()

            val newFlipped = tilesToWatch
                .filter {
                    if (tiles[it] == true) {
                        // its flipped
                        val neighbours = it.getNeighbors().count { tiles[it] ?: false }
                        neighbours in 1..2
                    } else {
                        val neighbours = it.getNeighbors().count { tiles[it] ?: false }
                        neighbours == 2
                    }
                }

            tiles = newFlipped.associateWith { true }
        }

        tiles.count { it.value }
            .solution(2)

    }
}

fun main(args: Array<String>) = solve<Day24>()
