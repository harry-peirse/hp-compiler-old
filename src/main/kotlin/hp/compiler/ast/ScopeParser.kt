package hp.compiler.ast

import hp.compiler.*
import java.lang.IllegalStateException

class ScopeParser(override val ast: Scope) : Parser<Scope> {
    override val finished get() = fsm.finished

    override fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: Parser<out AST>? = null

    private enum class State {
        Start,
        Defer,
        End
    }

    private val fsm = FSM<State, Lexeme>(State.Start, State.End) { state, lexeme ->
        if (DEBUG_LOGGING) println("${this.toString().padEnd(50)} ${state.toString().padEnd(11)} $lexeme")
        when (state) {
            State.Start -> handleStart(lexeme)
            State.Defer -> handleDefer(lexeme)
            State.End -> throw IllegalStateException()
        }
    }

    private fun handleStart(lexeme: Lexeme): State = when {
        lexeme.token == Token.NewLine || lexeme.token == Token.Semicolon -> State.Start
        lexeme.token == Token.RightBrace -> processEnd(lexeme)
        lexeme.token == Token.LeftBrace -> {
            val childAst = Scope(lexeme)
            child = ScopeParser(childAst)
            ast.children.add(childAst)
            State.Defer
        }
        lexeme.token == Token.EndOfInput ->
            if (ast.lexeme.token == Token.LeftBrace) throw CompilationException("Unexpected end of input", lexeme)
            else processEnd(lexeme)
        else -> {
            val childExpression = ScopedExpression(lexeme)
            child = StatementParser(childExpression)
            ast.children.add(childExpression)
            child?.input(lexeme)
            if(child!!.finished) processEnd(lexeme)
            State.Defer
        }
    }

    private fun handleDefer(lexeme: Lexeme): State {
        val child = child
        return if (child != null && !child.finished) {
            child.input(lexeme)
            if (child.finished && (lexeme.token == Token.EndOfInput || (child !is ScopeParser && lexeme.token == Token.RightBrace))) {
                processEnd(lexeme)
            } else {
                State.Defer
            }
        } else when {
            lexeme.token == Token.NewLine || lexeme.token == Token.Semicolon -> State.Defer
            lexeme.token == Token.RightBrace -> processEnd(lexeme)
            lexeme.token == Token.LeftBrace -> {
                val childScope = Scope(lexeme)
                this.child = ScopeParser(childScope)
                ast.children.add(childScope)
                State.Defer
            }
            lexeme.token == Token.EndOfInput ->
                if (ast.lexeme.token == Token.LeftBrace) throw CompilationException("Unexpected end of input", lexeme)
                else processEnd(lexeme)
            else -> {
                val childAst = ScopedExpression(lexeme)
                this.child = StatementParser(childAst)
                ast.children.add(childAst)
                this.child?.input(lexeme)
                if(this.child!!.finished) processEnd(lexeme)
                State.Defer
            }
        }
    }

    private fun processEnd(lexeme: Lexeme): State {
        ast.end = lexeme
        ast.linkHierarchy()
        return State.End
    }
}
