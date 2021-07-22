package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day18 : Day {
    override val day = 18

    sealed class Operation {
        abstract fun evaluate(): Long

        data class Number(val number: Int) : Operation() {
            override fun evaluate(): Long = number.toLong()
        }

        data class Addition(val left: Operation, val right: Operation) : Operation() {
            override fun evaluate(): Long = left.evaluate() + right.evaluate()
        }

        data class Multiplication(val left: Operation, val right: Operation) : Operation() {
            override fun evaluate(): Long = left.evaluate() * right.evaluate()
        }
    }

    fun parseExpression(expression: String): Operation {
        var stringLeft = expression
        val left = if (expression.endsWith(")")) {
            var depth = 0
            val subString = expression.takeLastWhile {
                if (it == '(') depth--
                if (it == ')') depth++
                depth >= 1
            }
            stringLeft = expression.dropLast(subString.length)

            parseExpression(subString.dropLast(1))
        } else if (expression.toIntOrNull() != null) {
            stringLeft = ""
            Operation.Number(expression.toInt())
        } else {
            val numberString = expression.substringAfterLast(" ")

            stringLeft = expression.removeSuffix(numberString)

            Operation.Number(numberString.toInt())
        }

        stringLeft = stringLeft
            .removeSuffix("(")
            .trim()

        if (stringLeft.isEmpty()) return left

        val operand = stringLeft.last()

        val right = parseExpression(stringLeft.dropLast(1).trim())

        return when (operand) {
            '+' -> Operation.Addition(left, right)
            '*' -> Operation.Multiplication(left, right)
            else -> error("invalid expression! $expression")
        }
    }

    fun parseExpression2(expression: String): Operation {
        val (expression, operands) = findTopLevelOperators(expression)
        return combineToOperation(expression, operands)
    }

    fun combineToOperation(operations: List<Operation>, operands: List<Char>): Operation {
        if (operations.size == 1) return operations.single()

        val highestPrecedenceIndex = if (operands.contains('*')) {
            operands.lastIndexOf('*')
        } else {
            0
        }

        val leftOperations = operations.take(highestPrecedenceIndex + 1)
        val leftOperators = operands.take(highestPrecedenceIndex)

        val rightOperations = operations.drop(highestPrecedenceIndex + 1)
        val rightOperators = operands.drop(highestPrecedenceIndex + 1)

        val left = combineToOperation(leftOperations, leftOperators)
        val right = combineToOperation(rightOperations, rightOperators)

        val operand = operands[highestPrecedenceIndex]
        return when(operand) {
            '+' -> Operation.Addition(left, right)
            '*' -> Operation.Multiplication(left, right)
            else -> error("invalid operand! $operand")
        }


    }

    fun findTopLevelOperators(expression: String): Pair<List<Operation>, List<Char>> {
        if (expression == "") return emptyList<Operation>() to emptyList()

        var stringLeft = expression
        val first = if (expression.startsWith("(")) {
            var depth = 0
            val subString = expression.takeWhile {
                if (it == '(') depth++
                if (it == ')') depth--
                depth >= 1
            }
            stringLeft = expression.drop(subString.length)

            parseExpression2(subString.drop(1))
        } else if (expression.toIntOrNull() != null) {
            stringLeft = ""
            Operation.Number(expression.toInt())
        } else {
            val numberString = expression.substringBefore(" ")

            stringLeft = expression.removePrefix(numberString)

            Operation.Number(numberString.toInt())
        }

        stringLeft = stringLeft
            .removePrefix(")")
            .trim()

        if (stringLeft.isEmpty()) return listOf(first) to emptyList()

        val operand = stringLeft.first()


        val (afterExpressions, afterOperations) = findTopLevelOperators(stringLeft.drop(1).trim())

        return (listOf(first) + afterExpressions) to (listOf(operand) + afterOperations)
    }

    override fun solvePart1() {
        loadInput()
            .map { parseExpression(it) }
            .map { it.evaluate() }
            .sum()
            .solution(1)
    }

    override fun solvePart2() {
        loadInput()
            .map { parseExpression2(it) }
            .map { it.evaluate() }
            .sum()
            .solution(2)
    }
}

fun main(args: Array<String>) = solve<Day18>()
