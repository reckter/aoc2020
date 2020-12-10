package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.replace
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day8 : Day {
    override val day = 8

    data class Instruction(
        val operation: Operation,
        val argument: Int
    ) {
        enum class Operation {
            acc,
            jmp,
            nop
        }
    }

    val instructions by lazy {
        loadInput()
            .map {
                val (opStr, argStr) = it.split(" ")
                val operation = when (opStr) {
                    "acc" -> Instruction.Operation.acc
                    "jmp" -> Instruction.Operation.jmp
                    "nop" -> Instruction.Operation.nop
                    else -> error("invalid operation string: $opStr")
                }
                val argument = argStr.toInt()
                Instruction(operation, argument)
            }
    }

    fun run(instructions: List<Instruction>): Pair<Boolean, Int> {
        var accumulator = 0
        var position = 0
        var done = false
        var exited = false

        val seen = mutableListOf<Int>()
        while (!done) {
            seen.add(position)
            val current = instructions[position]
            when (current.operation) {
                Instruction.Operation.acc -> accumulator += current.argument
                Instruction.Operation.jmp -> position += current.argument - 1
                Instruction.Operation.nop -> Unit
            }
            position++
            if (position in seen) done = true
            if (position >= instructions.size) {
                done = true
                exited = true
            }
        }
        return exited to accumulator
    }

    override fun solvePart1() {
        run(instructions)
            .second
            .solution(1)
    }

    override fun solvePart2() {
        instructions
            .asSequence()
            .mapIndexed { index, it ->
                index to it
            }
            .filter { (index, it) -> it.operation != Instruction.Operation.acc }
            .map { (index, it) ->
                val newInstruction = it.copy(
                    operation = when (it.operation) {
                        Instruction.Operation.jmp -> Instruction.Operation.nop
                        Instruction.Operation.nop -> Instruction.Operation.jmp
                        else -> error("invalid operation to flip! ${it.operation}")
                    }
                )
                instructions.replace(index, newInstruction)
            }
            .map { run(it) }
            .find { it.first }
            ?.second
            ?.solution(2)
    }
}

fun main(args: Array<String>) = solve<Day8>()
