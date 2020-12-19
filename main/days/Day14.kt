package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.math.pow

class Day14 : Day {
    override val day = 14

    fun applyMask(mask: String, to: Long): Long {
        val cutOut = mask.map { if(it == 'X') 1 else 0 }
            .joinToString("")
            .toLong(2)
        val cutIn = mask.map { if(it == 'X') 0 else 1 }
            .joinToString("")
            .toLong(2)

        val maskValue = mask.map { if(it == 'X') 0 else it}
            .joinToString("")
            .toLong(2)

        return (to and cutOut) or (cutIn and maskValue)
    }


    val maskLine = "mask = (?<mask>.*)".toRegex()
    val memoryLine = "mem\\[(?<address>\\d*)] = (?<value>\\d*)".toRegex()
    override fun solvePart1() {
        loadInput()
            .fold("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" to emptyMap<Int,Long>()) {(mask, memory), line ->
                if (maskLine.matches(line)) {
                    val newMask = maskLine.matchEntire(line)?.groups?.get("mask")?.value ?: error("no value")
                    return@fold newMask to memory
                }

                val match = memoryLine.matchEntire(line)?.groups ?: error("no memory match!")
                val address = match["address"]?.value?.toInt() ?: error("no value")
                val value = match["value"]?.value?.toLong() ?: error("no value")
                val writeValue = applyMask(mask, value)

                mask to (memory + (address to writeValue))
            }
            .second
            .values
            .sum()
            .solution(1)

    }

    fun generateAllPossibleNumbersFromMask(mask: String, address: Long): List<Long> {
        val overWrite = mask.map { if(it == '1') 1 else 0 }
            .joinToString("")
            .toLong(2)

        val cutOut = mask.map { if(it == '0') 1 else 0 }
            .joinToString("")
            .toLong(2)
        val cutIn = mask.map { if(it == 'X') 1 else 0 }
            .joinToString("")
            .toLong(2)

        val parts = mask.split("X")
        val max = (2.0).pow(parts.size - 1).toLong()
        val test =  (0 until max)
            .map { it.toString(2)}
            .map { it.padStart(parts.size - 1, '0')}
            .map { it + " " }
            .map {
                parts.zip(it.asIterable())
                    .joinToString("") { it.first + it.second}
            }
            .map{it.trim()}
        return test
            .map{ it.toLong(2)}
            .map { (address and cutOut) or overWrite or (it and cutIn)}
    }


    override fun solvePart2() {

        loadInput()
            .fold("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" to emptyMap<Long,Long>()) {(mask, memory), line ->
                if (maskLine.matches(line)) {
                    val newMask = maskLine.matchEntire(line)?.groups?.get("mask")?.value ?: error("no value")
                    return@fold newMask to memory
                }

                val match = memoryLine.matchEntire(line)?.groups ?: error("no memory match!")
                val address = match["address"]?.value?.toLong() ?: error("no value")
                val value = match["value"]?.value?.toLong() ?: error("no value")
                val addresses = generateAllPossibleNumbersFromMask(mask, address)

                mask to (memory + addresses.map { it to value})
            }
            .second
            .values
            .sum()
            .solution(2)

    }
}

fun main(args: Array<String>) = solve<Day14>()
