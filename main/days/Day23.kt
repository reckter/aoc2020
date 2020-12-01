package me.reckter.aoc.days

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.reckter.aoc.Day
import me.reckter.aoc.intcode.Action
import me.reckter.aoc.intcode.getAction
import me.reckter.aoc.intcode.startProgram
import me.reckter.aoc.intcode.toTape
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import java.math.BigInteger

class Day23 : Day {
    override val day = 23

    val tape by lazy {
        loadInput()
            .first()
            .toTape()
    }

    val size = 50
    val inputs = (0 until size)
        .map { Channel<BigInteger>(Channel.UNLIMITED) }

    val minusOneChannel = Channel<BigInteger>(Channel.BUFFERED)

    suspend fun receive(network: Int): BigInteger {
        return select {
            @Suppress("MoveLambdaOutsideParentheses")
            inputs[network].onReceive {
                it
            }
            minusOneChannel.onReceive {
                it
            }
        }
    }

    suspend fun sendPacket(network: Int, value: BigInteger) {
        inputs[network].send(value)
    }
    val outputs = (0 until size).map { mutableListOf<BigInteger>() }

    fun runNetwork(exitOnNat: Boolean): Int? {
        return runBlocking {
            launch {
                while (true) {
                    minusOneChannel.send((-1).toBigInteger())
                }
            }
            val pcs = (0 until size)
                .map { networkId ->
                    val inputChannel = Channel<BigInteger>(Channel.BUFFERED)
                    val waitingForInput = Channel<Unit>()
                    val runningResult = tape.startProgram(inputChannel, waitingForInput, true)

                    sendPacket(networkId, networkId.toBigInteger())
                    networkId to Triple(runningResult, inputChannel, waitingForInput)
                }

            while (pcs.any { !it.second.first.halted }) {
                pcs
                    .filter { !it.second.first.halted }
                    .forEach { (networkId, data) ->
                        val (runningResult, inputChannel, waitingForInput) = data

                        val action = runningResult.getAction(waitingForInput)
                        when (action) {
                            is Action.WaitForInput -> {
                                val input = receive(networkId)
//                                println("input for $networkId: $input")
                                inputChannel.send(input)
                            }
                            is Action.Output -> {
                                val outputList = outputs[networkId]
                                outputList.add(action.out)
                                println("output from $networkId: ${action.out}")
                                if (outputList.size == 3) {
                                    outputList.print("network")
                                    val receiver = outputList.first().toInt()
                                    if (receiver == 255) {
                                        if(exitOnNat) {
                                            this.coroutineContext.cancelChildren()
                                            return@runBlocking outputList[2].toInt()
                                        }
                                    } else {
                                        sendPacket(receiver, outputList[1])
                                        sendPacket(receiver, outputList[2])
                                        outputList.clear()
                                    }
                                }
                            }
                        }
                    }
            }

            this.coroutineContext.cancelChildren()

            null
        }
    }
    override fun solvePart1() {
        runNetwork(true)
            .solution(1)
    }

    override fun solvePart2() {
    }
}

fun main(args: Array<String>) = solve<Day23>()
