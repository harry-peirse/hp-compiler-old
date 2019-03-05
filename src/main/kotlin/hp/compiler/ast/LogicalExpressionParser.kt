package hp.compiler.ast

import hp.compiler.*

class LogicalExpressionParser(val logicalExpression: ScopedLogicalExpression) {

    val finished get() = fsm.finished

    fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: LogicalExpressionParser? = null
    private var logical: Logical? = null
    private var operator: LogicalOperator? = null

    private enum class State {
        Start,
        Value,
        Operator,
        Unary,
        Defer,
        End
    }

    private val fsm = FSM<State, Lexeme>(State.Start, State.End) { state, lexeme ->
        when (state) {
            State.Start -> handleStart(lexeme)
            State.Value -> handleValue(lexeme)
            State.Operator -> handleOperator(lexeme)
            State.Unary -> handleUnary(lexeme)
            State.Defer -> handleDefer(lexeme)
            State.End -> throw IllegalStateException()
        }
    }

    private fun handleStart(lexeme: Lexeme): State = when (lexeme.token) {
        Token.True, Token.False, Token.Identifier -> {
            logical = LogicalValue(lexeme)
            logicalExpression.child = logical
            State.Value
        }
        Token.Not -> {
            val unary = UnaryLogicalOperator(lexeme)
            logicalExpression.child = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedLogicalExpression(lexeme)
            child = LogicalExpressionParser(childExpression)
            logicalExpression.child = childExpression
            logical = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleValue(lexeme: Lexeme): State = when (lexeme.token) {
        Token.And, Token.Or, Token.XOr -> processOperator(lexeme)
        Token.RightParenthesis, Token.EndOfInput -> processEnd(lexeme)
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleOperator(lexeme: Lexeme): State = when (lexeme.token) {
        Token.True, Token.False, Token.Identifier -> {
            logical = LogicalValue(lexeme)
            (operator as BinaryLogicalOperator).right = logical
            State.Value
        }
        Token.Not -> {
            val unary = UnaryLogicalOperator(lexeme)
            (operator as BinaryLogicalOperator).right = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedLogicalExpression(lexeme)
            child = LogicalExpressionParser(childExpression)
            (operator as BinaryLogicalOperator).right = childExpression
            logical = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleUnary(lexeme: Lexeme): State = when (lexeme.token) {
        Token.True, Token.False, Token.Identifier -> {
            logical = LogicalValue(lexeme)
            (operator as UnaryLogicalOperator).child = logical
            State.Value
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedLogicalExpression(lexeme)
            child = LogicalExpressionParser(childExpression)
            (operator as UnaryLogicalOperator).child = childExpression
            logical = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleDefer(lexeme: Lexeme): State {
        val child = child
        return if (child != null && !child.finished) {
            child.input(lexeme)
            State.Defer
        } else when (lexeme.token) {
            Token.And, Token.Or, Token.XOr -> processOperator(lexeme)
            Token.RightParenthesis, Token.EndOfInput -> processEnd(lexeme)
            else -> throw CompilationException("Unexpected character", lexeme)
        }
    }

    private fun processOperator(lexeme: Lexeme): State {
        val binary = BinaryLogicalOperator(lexeme, left = logicalExpression.child)
        logicalExpression.child = binary
        operator = binary
        return State.Operator
    }

    private fun processEnd(lexeme: Lexeme): State {
        logicalExpression.end = lexeme
        return State.End
    }
}