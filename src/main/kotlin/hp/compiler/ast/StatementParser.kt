package hp.compiler.ast

import hp.compiler.*
import java.lang.IllegalStateException

class StatementParser(override val ast: ScopedExpression) : Parser<ScopedExpression> {

    val finished get() = fsm.finished

    override fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: ExpressionParser? = null

    private enum class State {
        Start,
        Declaration,
        Variable,
        Assignment,
        Expression,
        Defer,
        End
    }

    private val fsm = FSM<State, Lexeme>(State.Start, State.End) { state, lexeme ->
        println("${state.toString().padEnd(11)} $lexeme")
        when (state) {
            State.Start -> handleStart(lexeme)
            State.Declaration -> handleDeclaration(lexeme)
            State.Variable -> handleVariable(lexeme)
            State.Assignment -> handleAssignment(lexeme)
            State.Expression -> handleExpression(lexeme)
            State.Defer -> handleDefer(lexeme)
            State.End -> throw IllegalStateException()
        }
    }

    private fun handleStart(lexeme: Lexeme): State = when {
        lexeme.token == Token.Var -> {
            ast.expression = Declaration(lexeme)
            State.Declaration
        }
        lexeme.token.isVariable -> {
            ast.expression = Variable(lexeme)
            State.Variable
        }
        lexeme.token.isPrefixOperator -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            ast.expression = childExpression
            child?.input(lexeme)
            State.Defer
        }
        lexeme.token.isLiteral -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            ast.expression = childExpression
            child?.input(lexeme)
            State.Defer
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleDeclaration(lexeme: Lexeme): State = when {
        lexeme.token.isVariable -> {
            (ast.expression as Declaration).variable = Variable(lexeme)
            State.Variable
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleVariable(lexeme: Lexeme): State = when {
        lexeme.token.isAssignment -> when (ast.expression) {
            is Declaration -> {
                val childExpression = ScopedExpression(lexeme)
                (ast.expression as Declaration).expression = childExpression
                child = ExpressionParser(childExpression)
                State.Defer
            }
            is Variable -> {
                val assignment = Assignment(lexeme, variable = ast.expression)
                val childExpression = ScopedExpression(lexeme)
                ast.expression = assignment
                assignment.expression = childExpression
                child = ExpressionParser(childExpression)
                State.Defer
            }
            else -> throw CompilationException("Unexpected token", lexeme)
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleAssignment(lexeme: Lexeme): State = when {
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleExpression(lexeme: Lexeme): State = when {
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleDefer(lexeme: Lexeme): State = when {
        else -> throw CompilationException("Unexpected token", lexeme)
    }
}
