package hp.compiler.ast

import hp.compiler.*

class ArithemeticExpressionParser(val arithmeticExpression: ScopedArithmeticExpression) {

    val finished get() = fsm.finished

    fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: ArithemeticExpressionParser? = null
    private var arithmetic: Arithmetic? = null
    private var operator: ArithmeticOperator? = null

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
            arithmetic = ArithmeticValue(lexeme)
            arithmeticExpression.child = arithmetic
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = UnaryArithmeticOperator(lexeme)
            arithmeticExpression.child = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedArithmeticExpression(lexeme)
            child = ArithemeticExpressionParser(childExpression)
            arithmeticExpression.child = childExpression
            arithmetic = childExpression
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
            arithmetic = ArithmeticValue(lexeme)
            (operator as BinaryArithmeticOperator).right = arithmetic
            State.Value
        }
        Token.Plus, Token.Minus -> {
            val unary = UnaryArithmeticOperator(lexeme)
            (operator as BinaryArithmeticOperator).right = unary
            operator = unary
            State.Unary
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedArithmeticExpression(lexeme)
            child = ArithemeticExpressionParser(childExpression)
            (operator as BinaryArithmeticOperator).right = childExpression
            arithmetic = childExpression
            State.Defer
        }
        else -> throw CompilationException("Unexpected character", lexeme)
    }

    private fun handleUnary(lexeme: Lexeme): State = when (lexeme.token) {
        Token.Float, Token.Identifier -> {
            arithmetic = ArithmeticValue(lexeme)
            (operator as UnaryArithmeticOperator).child = arithmetic
            State.Value
        }
        Token.LeftParenthesis -> {
            val childExpression = ScopedArithmeticExpression(lexeme)
            child = ArithemeticExpressionParser(childExpression)
            (operator as UnaryArithmeticOperator).child = childExpression
            arithmetic = childExpression
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
        val ancestor = arithmeticExpression.child
        if (ancestor is BinaryArithmeticOperator && !ancestor.lexeme.token.highPriority && lexeme.token.highPriority) {
            val binary = BinaryArithmeticOperator(lexeme, left = ancestor.right)
            ancestor.right = binary
            operator = binary
        } else {
            val binary = BinaryArithmeticOperator(lexeme, left = arithmeticExpression.child)
            arithmeticExpression.child = binary
            operator = binary
        }
        return State.Operator
    }

    private fun processEnd(lexeme: Lexeme): State {
        arithmeticExpression.end = lexeme
        return State.End
    }
}