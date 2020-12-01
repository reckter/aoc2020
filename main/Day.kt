package me.reckter.aoc

import khttp.get
import java.io.File
import java.nio.file.Files

interface Day {

    val day: Int

    fun solvePart1()

    fun solvePart2()

    fun loadInput(part: Int = 0, trim: Boolean = true): List<String> {

        if (part != 0)
            return readLines("input/${day}_$part.txt")

        if (!Files.exists(File("input/$day.txt").toPath())) {
            println("downloading file for input $day...")

            val sessionFile = File("session-id.txt")
            if (!sessionFile.exists()) {
                error("input not there and could not download file, because session-id.txt is missing!")
            }

            val response = get(
                url = "http://adventofcode.com/2019/day/$day/input",
                cookies = mapOf("session" to sessionFile.readText())
            )

            if (response.statusCode != 200) {
                error("Failed to download part=$part!: ${response.jsonObject}")
            }

            val content = response.text
            File("input/$day.txt").writeText(content)
            println("download done")
        }

        return readLines("input/$day.txt")
            .let {
                if(trim) {
                    it.filter { it.isNotBlank() }
                } else
                    it
            }
    }
}

