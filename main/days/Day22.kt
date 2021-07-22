package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.toIntegers

class Day22 : Day {
    override val day = 22

    val startingDecks by lazy {
        loadInput( trim = false)
            .joinToString("\n")
            .split("\n\n")
            .associate {
                val lines = it.split("\n")
                val player = lines.first().removePrefix("Player ").replace(":", "").toInt()
                val deck = lines.drop(1).toIntegers()
                player to deck
            }
    }

    override fun solvePart1() {
        generateSequence(startingDecks[1]!! to startingDecks[2]!!) { (first, second) ->

            val zipped = first.zip(second)

            val tails = zipped.map { (first, second) ->
                if (first > second) {
                    listOf(first, second) to emptyList()
                } else {
                    emptyList<Int>() to listOf(second, first)
                }
            }

            val newFirst = first.drop(zipped.size) + tails.flatMap { it.first }
            val newSecond = second.drop(zipped.size) + tails.flatMap { it.second }
            newFirst to newSecond
        }
            .dropWhile { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .first()
            .let {
                val firstScore =
                    it.first.reversed().mapIndexed { index, i -> (index + 1) * i }.sum()
                val secondScore =
                    it.second.reversed().mapIndexed { index, i -> (index + 1) * i }.sum()

                firstScore + secondScore
            }
            .solution(1)
    }

    data class Game(
        val firstDeck: MutableList<Int>,
        val secondDeck: MutableList<Int>,
        var firstHand: Int? = null,
        var secondHand: Int? = null,
        val previousGameState : MutableSet<List<Int>> = mutableSetOf(),
    ) {
        fun isOver(): Boolean {
            return firstDeck.isEmpty() || secondDeck.isEmpty()
        }

        fun score(): Int {
            val firstScore =
                firstDeck.reversed().mapIndexed { index, i -> (index + 1) * i }.sum()
            val secondScore =
                secondDeck.reversed().mapIndexed { index, i -> (index + 1) * i }.sum()

            return firstScore + secondScore
        }
    }

    fun List<Game>.oneRound(): List<Game> {
        val currentGame = this.last()
        currentGame.firstHand = currentGame.firstDeck.removeFirst()
        currentGame.secondHand = currentGame.secondDeck.removeFirst()

        if (currentGame.firstDeck.size >= currentGame.firstHand!! && currentGame.secondDeck.size >= currentGame.secondHand!!) {
            return this + Game(
                currentGame.firstDeck.take(currentGame.firstHand!!).toMutableList(),
                currentGame.secondDeck.take(currentGame.secondHand!!).toMutableList()
            )
        } else {
            if (currentGame.firstHand!! > currentGame.secondHand!!) {
                currentGame.firstDeck.addAll(
                    listOf(
                        currentGame.firstHand!!,
                        currentGame.secondHand!!
                    )
                )
            } else {
                currentGame.secondDeck.addAll(
                    listOf(
                        currentGame.secondHand!!,
                        currentGame.firstHand!!
                    )
                )
            }
            return this
        }
    }

    fun List<Game>.handlePossibleGameEnd(): List<Game> {
        val currentGame = this.last()

        val stateHash = currentGame.firstDeck + -1 + currentGame.secondDeck
        if(currentGame.previousGameState.contains(stateHash)) {
            // player one won lol
            if (this.size == 1) return this
            val gameList = this.dropLast(1)
            val lastGame = gameList.last()
            lastGame.firstDeck.addAll(listOf(lastGame.firstHand!!, lastGame.secondHand!!))

            return gameList
        }

        currentGame.previousGameState.add(stateHash)

        if (currentGame.isOver()) {
            if (this.size == 1) return this
            val gameList = this.dropLast(1)
            val lastGame = gameList.last()
            if (currentGame.firstDeck.size == 0) {
                // second player won
                lastGame.secondDeck.addAll(listOf(lastGame.secondHand!!, lastGame.firstHand!!))
            } else {
                lastGame.firstDeck.addAll(listOf(lastGame.firstHand!!, lastGame.secondHand!!))
            }
            return gameList
        }
        return this
    }

    override fun solvePart2() {
        var gameList =
            listOf(Game(startingDecks[1]!!.toMutableList(), startingDecks[2]!!.toMutableList()))

        while (!gameList.first().isOver()) {
            gameList = gameList
                .oneRound()
                .handlePossibleGameEnd()
        }

        gameList.first().score().solution(2)
    }
}

fun main(args: Array<String>) = solve<Day22>()
