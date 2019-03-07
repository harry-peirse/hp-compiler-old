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
        Defer,
        End
    }

    private val fsm = FSM<State, Lexeme>(State.Start, State.End) { state, lexeme ->
        println("${state.toString().padEnd(11)} $lexeme")
        when (state) {
            State.Start -> handleStart(lexeme)
            State.Declaration -> handleDeclaration(lexeme)
            State.Variable -> handleVariable(lexeme)
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
        else -> {
            child = ExpressionParser(ast)
            child?.input(lexeme)
            State.Defer
        }
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
        else -> {
            child = ExpressionParser(ast)
            child?.input(ast.expression!!.lexeme)
            child?.input(lexeme)
            State.Defer
        }
    }

    private fun handleDefer(lexeme: Lexeme): State {
            val child = child
        return if (child != null && !child.finished) {
            child.input(lexeme)
            if(child.finished && (lexeme.token == Token.EndOfInput ||
                            lexeme.token == Token.NewLine ||
                            lexeme.token == Token.Semicolon)) {
                processEnd(lexeme)
            } else {
                State.Defer
            }
        } else when (lexeme.token) {
            Token.Semicolon, Token.NewLine, Token.EndOfInput -> processEnd(lexeme)
            else -> throw CompilationException("Unexpected token", lexeme)
        }
    }

    private fun processEnd(lexeme: Lexeme): State {
        ast.end = lexeme
        ast.linkHierarchy()
        return State.End
    }
}
