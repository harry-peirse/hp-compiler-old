package hp.compiler.ast

import hp.compiler.*

class ExpressionParser(val expression: ScopedExpression) {

    val finished get() = fsm.finished
    var value: Expression? = null
    var operator: Operator? = null

    fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: ExpressionParser? = null

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
        Token.Number, Token.Identifier -> {
            value = Value(lexeme)
            expression.child = value
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = UnaryOperator(lexeme)
            expression.child = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            expression.child = childExpression
            value = childExpression
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
        Token.Number, Token.Identifier -> {
            value = Value(lexeme)
            (operator as BinaryOperator).right = value
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = UnaryOperator(lexeme)
            (operator as BinaryOperator).right = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (operator as BinaryOperator).right = childExpression
            value = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleUnary(lexeme: Lexeme): State = when (lexeme.token) {
        Token.Number, Token.Identifier -> {
            value = Value(lexeme)
            (operator as UnaryOperator).child = value
            State.Value
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (operator as UnaryOperator).child = childExpression
            value = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleDefer(lexeme: Lexeme): State {
        val child = child
        return if(child != null && !child.finished) {
            child.input(lexeme)
            State.Defer
        } else when (lexeme.token) {
            Token.Plus, Token.Minus, Token.Times, Token.Divide -> processOperator(lexeme)
            Token.RightParenthesis, Token.EndOfInput -> processEnd(lexeme)
            else -> throw CompilationException("Unexpected character", lexeme)
        }
    }

    private fun processOperator(lexeme: Lexeme): State {
        val ancestor = expression.child
        if (ancestor is BinaryOperator && !ancestor.lexeme.token.highPriority && lexeme.token.highPriority) {
            val binary = BinaryOperator(lexeme, left = ancestor.right)
            ancestor.right = binary
            operator = binary
        } else {
            val binary = BinaryOperator(lexeme, left = expression.child)
            expression.child = binary
            operator = binary
        }
        return State.Operator
    }

    private fun processEnd(lexeme: Lexeme): State {
        expression.endLexeme = lexeme
        return State.End
    }
}