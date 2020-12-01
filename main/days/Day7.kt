package me.reckter.aoc.days

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.runBlocking
import me.reckter.aoc.Day
import me.reckter.aoc.channelOf
import me.reckter.aoc.intcode.RunningResult
import me.reckter.aoc.intcode.receive
import me.reckter.aoc.intcode.runProgram
import me.reckter.aoc.intcode.startProgram
import me.reckter.aoc.permutations
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers
import java.math.BigInteger

class Day7 : Day {
    override val day = 7

    val code = run {
        loadInput()
            .first()
            .split(",")
            .toIntegers()
            .map { it.toBigInteger() }
    }

    fun calculatePass(settings: List<Int>): BigInteger {
        return settings.fold(BigInteger.ZERO) { acc, cur ->
            val output = code.runProgram(channelOf(cur.toBigInteger(), acc))
            output.output.single()
        }
    }

    fun wireProgram(settings: List<Int>): BigInteger {
        val input = Channel<BigInteger>(100)
        val runningResult = settings.fold(RunningResult(
            false,
            input,
            emptyMap(),
            0
        )) { result, cur ->
            result.output.sendBlocking(cur.toBigInteger())
            val res = code.startProgram(result.output)
            res
        }

        input.sendBlocking(BigInteger.ZERO)
        var result = BigInteger.ZERO
        runBlocking {
            while (true) {
                val msg = runningResult.receive() ?: break
                input.send(msg)
                result = msg
            }
        }
        return result
    }

    override fun solvePart1() {
        (0..4)
            .toList()
            .permutations()
            .map {
                calculatePass(it)
            }
            .max()
            .solution(1)
    }

    override fun solvePart2() {
        (5..9)
            .toList()
            .permutations()
            .map {
                wireProgram(it)
            }
            .max()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day7>()
