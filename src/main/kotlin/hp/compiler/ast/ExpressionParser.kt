package hp.compiler.ast

import hp.compiler.*

class ExpressionParser(val scopedExpression: ScopedExpression) {

    val finished get() = fsm.finished

    fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: ExpressionParser? = null
    private var currentOperator: Operator? = null

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
        Token.Float, Token.Identifier -> {
            scopedExpression.child = Value(lexeme)
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = PrefixOperator(lexeme)
            scopedExpression.child = unary
            currentOperator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            scopedExpression.child = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleValue(lexeme: Lexeme): State = when (lexeme.token) {
        Token.Plus, Token.Minus, Token.Times, Token.Divide -> processOperator(lexeme)
        Token.RightParenthesis, Token.EndOfInput -> processEnd(lexeme)
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleOperator(lexeme: Lexeme): State = when (lexeme.token) {
        Token.Float, Token.Identifier -> {
            (currentOperator as BinaryOperator).right = Value(lexeme)
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = PrefixOperator(lexeme)
            (currentOperator as BinaryOperator).right = unary
            currentOperator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (currentOperator as BinaryOperator).right = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleUnary(lexeme: Lexeme): State = when (lexeme.token) {
        Token.Float, Token.Identifier -> {
            (currentOperator as PrefixOperator).child = Value(lexeme)
            State.Value
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (currentOperator as PrefixOperator).child = childExpression
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
            Token.Plus, Token.Minus, Token.Times, Token.Divide -> processOperator(lexeme)
            Token.RightParenthesis, Token.EndOfInput -> processEnd(lexeme)
            else -> throw CompilationException("Unexpected character", lexeme)
        }
    }

    private fun processOperator(lexeme: Lexeme): State {
        val ancestor = scopedExpression.child
        if (ancestor is BinaryOperator && !ancestor.lexeme.token.highPriority && lexeme.token.highPriority) {
            val binary = BinaryOperator(lexeme, left = ancestor.right)
            ancestor.right = binary
            currentOperator = binary
        } else {
            val binary = BinaryOperator(lexeme, left = scopedExpression.child)
            scopedExpression.child = binary
            currentOperator = binary
        }
        return State.Operator
    }

    private fun processEnd(lexeme: Lexeme): State {
        scopedExpression.end = lexeme
        return State.End
    }
}