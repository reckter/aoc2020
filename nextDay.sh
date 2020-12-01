#!/bin/bash


echo "day:"
read day
file="Day$day.kt"

cat > "main/days/$file" << EOF
package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day$day : Day {
    override val day = $day

    override fun solvePart1() {

    }

    override fun solvePart2() {

    }
}

fun main(args: Array<String>) = solve<Day$day>()
EOF
