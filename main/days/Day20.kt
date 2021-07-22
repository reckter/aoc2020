package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.cords.d2.Cord2D
import me.reckter.aoc.cords.d2.getNeighbors
import me.reckter.aoc.cords.d2.plus
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAtEmptyLine

class Day20 : Day {
    override val day = 20

    val images: Map<Int, List<List<Boolean>>> by lazy {
        loadInput( trim = false)
            .splitAtEmptyLine()
            .filter { it.toList().isNotEmpty() }
            .map {
                val id = it.first()
                    .removePrefix("Tile ")
                    .removeSuffix(":")
                    .toInt()

                val pixel = it.drop(1)
                    .map { it.map { it == '#' } }
                id to pixel
            }
            .toMap()
    }

    enum class Operation(val doIt: (List<List<Boolean>>) -> List<List<Boolean>>) {
        flipX({ it.map { it.reversed() } }),
        flipY({ it.reversed() }),
        rotate90({ it.rotate().rotate().rotate() }),
        rotate180({ it.rotate().rotate() }),
        rotate270({ it.rotate() }),
        noOp({ it });
    }

    val allFlips = listOf(Operation.flipX, Operation.flipY, Operation.noOp)
    val allRotations =
        listOf(Operation.rotate90, Operation.rotate180, Operation.rotate270, Operation.noOp)

    enum class Direction {
        up,
        right,
        down,
        left;

        fun getPos(): Cord2D<Int> = when (this) {
            up -> Cord2D(0, 1)
            right -> Cord2D(1, 0)
            down -> Cord2D(0, -1)
            left -> Cord2D(-1, 0)
        }
    }

    fun fitsAgainst(
        compareAgainst: List<List<Boolean>>,
        pixel: List<List<Boolean>>,
        direction: Direction
    ): Boolean {
        return when (direction) {
            Direction.up -> compareAgainst.last() == pixel.first()
            Direction.right -> compareAgainst.map { it.first() } == pixel.map { it.last() }
            Direction.down -> compareAgainst.first() == pixel.last()
            Direction.left -> compareAgainst.map { it.last() } == pixel.map { it.first() }
        }
    }

    val map by lazy {
        val tmp = images.entries.map { it.key to it.value }
        val queue = ArrayDeque<Pair<Int, List<List<Boolean>>>>(tmp)

        val ret = mutableMapOf<Cord2D<Int>, Pair<Int, List<List<Boolean>>>>()
        val first = queue.removeFirst()

        ret[Cord2D(0, 0)] = first.first to first.second

        while (queue.isNotEmpty()) {
            val (id, pixel) = queue.removeFirst()

            val possiblePositions = ret
                .keys
                .flatMap { it.getNeighbors(true) }
                .filter { it !in ret.keys }
                .distinct()

            val allRotations = allFlips
                .flatMap { flip ->
                    allRotations.map { rotation ->
                        rotation
                            .doIt(
                                flip.doIt(pixel)
                            )
                    }
                }

            val fit = possiblePositions
                .asSequence()
                .map { position ->
                    val neigbours =
                        Direction
                            .values()
                            .map { it to (it.getPos() + position) }
                            .filter { ret.containsKey(it.second) }
                            .map { ret[it.second]!!.second to it.first }

                    val found = allRotations
                        .find { rotation ->
                            neigbours.all { fitsAgainst(it.first, rotation, it.second) }
                        }

                    found?.let { position to it }
                }
                .filterNotNull()
                .firstOrNull()

            if (fit == null) {
                queue.add(id to pixel)
                continue
            }

            ret[fit.first] = id to fit.second
        }

        ret
    }

    override fun solvePart1() {
        val minX = map.keys
            .map { it.x }
            .minOrNull() ?: error("no min X")
        val minY = map.keys
            .map { it.y }
            .minOrNull() ?: error("no min Y")
        val maxX = map.keys
            .map { it.x }
            .maxOrNull() ?: error("no min X")
        val maxY = map.keys
            .map { it.y }
            .maxOrNull() ?: error("no min Y")


        listOf(
            Cord2D(minX, minY),
            Cord2D(maxX, minY),
            Cord2D(minX, maxY),
            Cord2D(maxX, maxY),
        )
            .mapNotNull {
                map[it]?.first?.toLong()
            }
            .reduce { acc, i -> acc * i }
            .solution(1)
    }


    fun findInMap(map: List<List<Boolean>>, monster: List<List<Boolean>>): Int {
        return (0..map.size - monster.size)
            .sumOf { y ->
                (0..map[y].size - monster.first().size).count { x ->
                    monster.mapIndexed { monsterY, monsterSlices ->
                        monsterSlices.mapIndexed { monsterX, pixel ->
                            if(pixel) map[y + monsterY][x + monsterX] else true
                        }
                            .all { it }
                    }
                        .all { it}
                }
            }
    }

    override fun solvePart2() {
        val minX = map.keys
            .map { it.x }
            .minOrNull() ?: error("no min X")
        val minY = map.keys
            .map { it.y }
            .minOrNull() ?: error("no min Y")
        val maxX = map.keys
            .map { it.x }
            .maxOrNull() ?: error("no max X")
        val maxY = map.keys
            .map { it.y }
            .maxOrNull() ?: error("no max Y")

        val tileSize = map.values.first().second.size


        val wholeMap = (maxY downTo minY).flatMap { y ->
            (1 until tileSize - 1).map { tileNumber ->
                (minX..maxX).flatMap { x ->
                    val tile = map[Cord2D(x, y)]!!
                    tile.second[tileNumber].drop(1).dropLast(1)
                }
            }
        }

        val monsterString = listOf(
            "                  # ",
            "#    ##    ##    ###",
            " #  #  #  #  #  #   "
        )

        val monster = monsterString.map {it.map { it == '#'}}

        val allMonsterRotations = allFlips
            .flatMap { flip ->
                allRotations.map { rotation ->
                    rotation
                        .doIt(
                            flip.doIt(monster)
                        )
                }
            }
            .distinct()




        val foundMonster =allMonsterRotations
            .sumOf {
                findInMap(wholeMap, it)
            }

        val monsterValue = monster.sumOf { it.count { it } }

        val allRipples = wholeMap.sumOf {it.count{it}}

        (allRipples - (foundMonster * monsterValue))
            .solution(2)


    }
}

fun main(args: Array<String>) = solve<Day20>()

fun List<List<Boolean>>.print() {
    println(this.joinToString("\n") { it.joinToString("") { if (it) "#" else "." } })
}

fun List<List<Boolean>>.rotate(): List<List<Boolean>> {
    return this.first().indices.reversed().map { index -> this.map { it[index] } }
}
