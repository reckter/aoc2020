package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.matchWithRegexAndParse
import me.reckter.aoc.repeatToSequence
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.math.absoluteValue
import kotlin.math.floor

class Day22 : Day {
    override val day = 22

    sealed class Action() {
        object Reverse : Action()
        data class DealWithIncrement(val increment: Int) : Action()
        data class Cut(val offset: Int) : Action()
    }

    val actions by lazy {
        loadInput()
            .matchWithRegexAndParse<Action>(
                "deal into new stack".toRegex() to { _ -> Action.Reverse },
                "deal with increment (.*)".toRegex() to { (increment) ->
                    Action.DealWithIncrement(
                        increment.toInt()
                    )
                },
                "cut (.*)".toRegex() to { (offset) -> Action.Cut(offset.toInt()) }
            )
    }

    override fun solvePart1() {
        actions
            .fold((0 until 10007).toList()) { cards, action ->
                when (action) {
                    Action.Reverse -> cards.reversed()
                    is Action.DealWithIncrement -> cards
                        .mapIndexed { index, it -> ((index * action.increment) % cards.size) to it }
                        .sortedBy { it.first }
                        .map { it.second }
                    is Action.Cut -> {
                        val offset = if (action.offset > 0)
                            action.offset
                        else
                            cards.size + action.offset

                        cards.drop(offset) + cards.take(offset)
                    }
                }
            }
            .indexOf(2019)
            .solution(1)
    }

    fun reverseShuffle(goal: Long, findCardOnIndex: Long, deckSize: Long, repeatingFor: Long): Long {
        val sequence = actions
            .repeatToSequence(repeatingFor)
            .flatten()
        val shufflelist = mutableListOf<Long>()

        var index = findCardOnIndex
        val size = deckSize
        var shuffles = 0L

        val iterator = sequence.iterator()
        while(true) {
            shuffles++

            index = when (val action = iterator.next()) {
                Action.Reverse -> size - index
                is Action.DealWithIncrement -> {
                    dealWithIncrementReverese(index, size, action)
                }
                is Action.Cut -> {
                    cutReverse(action, size, index)
                }
            }
            shufflelist.add(index)
            if((shufflelist[0] == index && shuffles % actions.size == 0L)|| shuffles == goal) {
                break
            }
        }

        val loopStart = 0

        val loopLength = shuffles - loopStart

        println("loop found from $loopStart to $shuffles, $loopLength long")

        val offset = (goal - loopStart) % loopLength

        return shufflelist[offset.toInt()]
    }

    private fun cutReverse(
        action: Action.Cut,
        size: Long,
        index: Long
    ): Long {
        val offset = if (action.offset > 0)
            size - action.offset.toLong()
        else
            action.offset.toLong().absoluteValue

        return if (index < offset)
            size - offset + index
        else
            index - offset
    }

    private fun dealWithIncrementReverese(
        index: Long,
        size: Long,
        action: Action.DealWithIncrement
    ): Long {
        return (generateSequence(0) {
            it + 1
        }
            .map { (index + it * size).toDouble() / action.increment.toDouble() }
            .find { (it - floor(it)) < 0.0001 }
            ?.toLong()
            ?: error("could not reverse dealWith Increment $action"))
    }

    override fun solvePart2() {

        reverseShuffle(
            goal = 101741582076661 * actions.size,
            findCardOnIndex = 2020,
            deckSize = 119315717514047,
            repeatingFor = 101741582076661
        )
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day22>()
