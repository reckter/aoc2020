package me.reckter.aoc.days

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.reckter.aoc.Context
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.RunningResult
import me.reckter.aoc.intcode.receive
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.intcode.startProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import java.math.BigInteger
import kotlin.math.sign

class Day13 : Day {
    override val day = 13

    val code = run {
        loadInput()
            .first()
            .toTape()
    }

    enum class Tile {
        empty,
        wall,
        block,
        paddle,
        ball
    }

    override fun solvePart1() {
        val result = code.runProgram()
        val map = result.output
            .map { it.toInt() }
            .chunked(3)
            .fold(mapOf<Pair<Int, Int>, Tile>()) { map, instruction ->
                map + ((instruction[0] to instruction[1]) to Tile.values()[instruction[2]])
            }
        map.values
            .count { it == Tile.block }
            .solution(1)
    }

    var score = 0
    var map = mutableMapOf<Pair<Int, Int>, Tile>()

    suspend fun handleOutput(result: RunningResult) {
        val x = result.receive()?.toInt() ?: return
        val y = result.receive()?.toInt() ?: return
        val tileId = result.receive()?.toInt() ?: return

        handleOutput(x, y, tileId)
    }

    fun handleOutput(x: Int, y: Int, tileId: Int) {
        if (x == -1 && y == 0) {
            score = tileId
            return
        }

        map[x to y] = Tile.values()[tileId]
    }

    fun drawMap() {
        val minX = map.keys
            .map { it.first }
            .min() ?: 0
        val maxX = map.keys
            .map { it.first }
            .max() ?: 1
        val minY = map.keys
            .map { it.second }
            .min() ?: 0
        val maxY = map.keys
            .map { it.second }
            .max() ?: 1

        val string = (minY..maxY).joinToString("\n") { y ->
            (minX..maxX).joinToString("") { x ->
                when (map[x to y] ?: Tile.empty) {
                    Tile.empty -> " "
                    Tile.wall -> "█"
                    Tile.block -> "X"
                    Tile.paddle -> "-"
                    Tile.ball -> "O"
                }
            }
        }

        println("\nscore: $score")
        println(string)
    }

    fun loadSavedInput(): List<Int> {
        return loadInput(2)
            .first()
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .map { it.trim() }
            .toIntegers()
    }

    fun nextMove(input: Channel<Int>) {
        val ballX = map.entries.find { it.value == Tile.ball }?.key?.first ?: return
        val paddleX = map.entries.find { it.value == Tile.paddle }?.key?.first ?: return

        input.sendBlocking((ballX - paddleX).sign)
    }

    override fun solvePart2() {
        val game = (code + (0L to BigInteger.valueOf(2)))

        val input = Channel<Int>()
        val waitingForInput = Channel<Unit>()
        val gameInput = input
            .map {
                it.toBigInteger()
            }

        val result = runBlocking { game.startProgram(gameInput, waitingForInput) }

        data class Action(
            val waitForInput: Boolean = false,
            val output: Int? = null,
            val end: Boolean = false
        )

        suspend fun receive(): Action {
            return select<Action> {
                result.output.onReceive {
                    Action(output = it.toInt())
                }
                waitingForInput.onReceive {
                    Action(waitForInput = true)
                }
                result.haltedChannel.onReceive {
                    Action(end = true)
                }
            }
        }

        runBlocking {
            var end = false
            var output = listOf<Int>()

            while (!end) {
                val action = receive()
                when {
                    action.waitForInput -> {
                        if (!Context.testMode)
                            drawMap()
                        nextMove(input)
                    }
                    action.end -> {
                        end = true
                    }
                    action.output != null -> {
                        output += action.output
                        if (output.size == 3) {
                            val (x, y, tileId) = output
                            handleOutput(x, y, tileId)
                            output = emptyList()
                        }
                    }
                }
            }
        }
        drawMap()

        score.solution(2)
    }
}

fun main(args: Array<String>) = solve<Day13>()
