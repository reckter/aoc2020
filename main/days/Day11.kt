package me.reckter.aoc.days

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.receive
import me.reckter.aoc.intcode.startProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.math.BigInteger

class Day11 : Day {
    override val day = 11

    enum class Color {
        black,
        white
    }

    enum class Rotation(val step: (cur: Pair<Int, Int>) -> Pair<Int, Int>) {
        up({ it.copy(second = it.second - 1) }),
        right({ it.copy(first = it.first + 1) }),
        down({ it.copy(second = it.second + 1) }),
        left({ it.copy(first = it.first - 1) });

        fun rotateRight(): Rotation = values()[(this.ordinal + 1) % values().size]
        fun rotateLeft(): Rotation = values()[(this.ordinal - 1 + values().size) % values().size]
    }

    data class PointData(
        val color: Color
    )

    fun paint(startMap: Map<Pair<Int, Int>, Color>): Map<Pair<Int, Int>, Color> {
        val map = startMap.toMutableMap()
        var position = 0 to 0
        var rotation = Rotation.up

        val code = loadInput()
            .first()
            .toTape()

        val input = Channel<BigInteger>(100)
        val result = runBlocking { code.startProgram(input) }

        runBlocking {
            try {
                while (!result.halted) {
                    val color = map.getOrDefault(position, Color.black)

                    input.send(color.ordinal.toBigInteger())

                    if (result.halted) break

                    val toColor = result.receive() ?: break

                    map[position] = Color.values()[toColor.toInt()]
                    val direction = result.receive() ?: break
                    rotation = when (direction.toInt()) {
                        0 -> rotation.rotateLeft()
                        1 -> rotation.rotateRight()
                        else -> error("invalid rotation $direction")
                    }
                    position = rotation.step(position)
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Channel closed; halted: ${result.halted}")
            }
        }

        return map
    }


    fun Map<Pair<Int, Int>, Color>.printMap(): String {
        val minX = this.keys
            .map { it.first }
            .min() ?: error("no min")
        val minY = this.keys
            .map { it.second }
            .min() ?: error("no min")
        val maxX = this.keys
            .map { it.first }
            .max() ?: error("no min")
        val maxY = this.keys
            .map { it.second }
            .max() ?: error("no min")

        return ("\n" + (minY..maxY).joinToString("\n") { y ->
            (minX..maxX).joinToString("") { x ->
                when(this.getOrDefault(x to y, Color.black)) {
                    Color.black -> " "
                    Color.white -> "X"
                }
            }
        })

    }

    override fun solvePart1() {
        paint(emptyMap())
            .keys
            .count()
            .solution(1)
    }

    override fun solvePart2() {
        paint(mapOf((0 to 0) to Color.white))
            .printMap()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day11>()
