package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAt
import kotlin.reflect.full.memberProperties

class Day4 : Day {
    override val day = 4

    data class Passport(
        val byr: String?,
        val iyr: String?,
        val eyr: String?,
        val hgt: String?,
        val hcl: String?,
        val ecl: String?,
        val pid: String?,
        val cid: String?
    )

    val requiredFields = listOf("byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid")

    val passports by lazy {
        loadInput(trim = false)
            .splitAt { it == "" }
            .map { it.joinToString(" ") }
            .map {
                it
                    .split(" ")
                    .map {
                        val (key, value) = it.split(":")
                        key to value
                    }
                    .toMap()
            }
            .map {
                Passport(
                    it["byr"],
                    it["iyr"],
                    it["eyr"],
                    it["hgt"],
                    it["hcl"],
                    it["ecl"],
                    it["pid"],
                    it["cid"]
                )
            }
    }

    override fun solvePart1() {
        passports
            .count { passport ->
                val map = passport.asMap()
                requiredFields.all { map.containsKey(it) && map[it] != null }
            }
            .solution(1)
    }

    override fun solvePart2() {
        passports
            .filter { passport ->
                val map = passport.asMap()
                requiredFields.all { map.containsKey(it) && map[it] != null }
            }
            .filter {
                val year = it.byr?.toInt() ?: return@filter false
                year in 1920..2002
            }
            .filter {
                val year = it.iyr?.toInt() ?: return@filter false
                year in 2010..2020
            }
            .filter {
                val year = it.eyr?.toInt() ?: return@filter false
                year in 2020..2030
            }
            .filter {
                val ending = it.hgt?.takeLast(2)
                if(ending !in listOf("in", "cm")) return@filter false
                val isCm = it.hgt?.endsWith("cm") ?: return@filter false

                val size = it.hgt.dropLast(2).toInt()
                if(isCm) {
                    size in 150..193
                } else {
                    size in 59..76
                }
            }
            .filter {
                it.hcl?.matches("#[0-9a-f]{6}".toRegex()) ?: false
            }
            .filter{
                it.ecl in listOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")
            }
            .filter {
                it.pid?.matches("\\d{9}".toRegex()) ?: false
            }
            .count()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day4>()

inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
    val props = T::class.memberProperties.associateBy { it.name }
    return props.keys.associateWith { props[it]?.get(this) }
}
