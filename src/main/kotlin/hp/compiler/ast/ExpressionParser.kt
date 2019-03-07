package hp.compiler.ast

import hp.compiler.*

class ExpressionParser(override val ast: ScopedExpression) : Parser<ScopedExpression> {

    val finished get() = fsm.finished

    override fun input(lexeme: Lexeme) {
        if (!finished) {
            fsm.input(lexeme)
        }
    }

    private var child: ExpressionParser? = null
    private var currentOperator: AST? = null

    private enum class State {
        Start,
        Literal,
        Variable,
        Operator,
        Prefix,
        Postfix,
        Defer,
        End
    }

    private val fsm = FSM<State, Lexeme>(State.Start, State.End) { state, lexeme ->
        println("${state.toString().padEnd(11)} $lexeme")
        when (state) {
            State.Start -> handleStart(lexeme)
            State.Literal -> handleLiteral(lexeme)
            State.Variable -> handleVariable(lexeme)
            State.Operator -> handleOperator(lexeme)
            State.Prefix -> handlePrefix(lexeme)
            State.Postfix -> handlePostfix(lexeme)
            State.Defer -> handleDefer(lexeme)
            State.End -> throw IllegalStateException()
        }
    }

    private fun handleStart(lexeme: Lexeme): State = when {
        lexeme.token.isLiteral -> {
            ast.expression = Literal(lexeme)
            State.Literal
        }
        lexeme.token.isVariable -> {
            ast.expression = Variable(lexeme)
            State.Variable
        }
        lexeme.token.isPrefixOperator -> {
            val unary = PrefixOperator(lexeme)
            ast.expression = unary
            currentOperator = unary
            State.Prefix
        }
        lexeme.token == Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            ast.expression = childExpression
            State.Defer
        }
        lexeme.token == Token.NewLine -> {
            if(ast.lexeme.token == Token.LeftParenthesis || ast.lexeme.token.isAssignment) State.Start
            else processEnd(lexeme)
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleLiteral(lexeme: Lexeme): State = when {
        lexeme.token.isBinaryOperator -> processOperator(lexeme)
        lexeme.token == Token.RightParenthesis || lexeme.token == Token.EndOfInput || lexeme.token == Token.Semicolon -> processEnd(lexeme)
        lexeme.token == Token.NewLine -> {
            if (ast.lexeme.token == Token.LeftParenthesis) State.Literal
            else processEnd(lexeme)
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleVariable(lexeme: Lexeme): State = when {
        lexeme.token.isBinaryOperator -> processOperator(lexeme)
        lexeme.token.isPostfixOperator -> processPostfix(lexeme)
        lexeme.token == Token.RightParenthesis || lexeme.token == Token.EndOfInput || lexeme.token == Token.Semicolon -> processEnd(lexeme)
        lexeme.token == Token.NewLine -> {
            if (ast.lexeme.token == Token.LeftParenthesis) State.Variable
            else processEnd(lexeme)
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleOperator(lexeme: Lexeme): State = when {
        lexeme.token.isLiteral -> {
            (currentOperator as BinaryOperator).right = Literal(lexeme)
            State.Literal
        }
        lexeme.token.isVariable -> {
            (currentOperator as BinaryOperator).right = Variable(lexeme)
            State.Variable
        }
        lexeme.token.isPrefixOperator -> {
            val unary = PrefixOperator(lexeme)
            (currentOperator as BinaryOperator).right = unary
            currentOperator = unary
            State.Prefix
        }
        lexeme.token == Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (currentOperator as BinaryOperator).right = childExpression
            State.Defer
        }
        lexeme.token == Token.NewLine -> State.Operator
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handlePrefix(lexeme: Lexeme): State = when {
        lexeme.token.isLiteral -> {
            if ((currentOperator as PrefixOperator).lexeme.token == Token.Increment || (currentOperator as PrefixOperator).lexeme.token == Token.Decrement) {
                throw CompilationException("Illegal operator", lexeme)
            }
            (currentOperator as PrefixOperator).child = Literal(lexeme)
            State.Literal
        }
        lexeme.token.isVariable -> {
            (currentOperator as PrefixOperator).child = Variable(lexeme)
            State.Variable
        }
        lexeme.token == Token.LeftParenthesis -> {
            val childExpression = ScopedExpression(lexeme)
            child = ExpressionParser(childExpression)
            (currentOperator as PrefixOperator).child = childExpression
            State.Defer
        }
        lexeme.token == Token.NewLine -> State.Prefix
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handlePostfix(lexeme: Lexeme): State = when {
        lexeme.token.isBinaryOperator -> processOperator(lexeme)
        lexeme.token == Token.RightParenthesis || lexeme.token == Token.EndOfInput || lexeme.token == Token.Semicolon -> processEnd(lexeme)
        lexeme.token == Token.NewLine -> {
            if (ast.lexeme.token == Token.LeftParenthesis) State.Postfix
            else processEnd(lexeme)
        }
        else -> throw CompilationException("Unexpected token", lexeme)
    }

    private fun handleDefer(lexeme: Lexeme): State {
        val child = child
        return if (child != null && !child.finished) {
            child.input(lexeme)
            if (child.finished && (lexeme.token == Token.EndOfInput ||
                            lexeme.token == Token.NewLine ||
                            lexeme.token == Token.Semicolon)) {
                processEnd(lexeme)
            } else {
                State.Defer
            }
        } else when (lexeme.token) {
            Token.Plus, Token.Minus, Token.Times, Token.Divide -> processOperator(lexeme)
            Token.Increment, Token.Decrement -> processPostfix(lexeme)
            Token.Semicolon, Token.EndOfInput -> {
                if (ast.lexeme.token != Token.LeftParenthesis) processEnd(lexeme)
                else throw CompilationException("Unexpected end of expression", lexeme)
            }
            Token.NewLine -> {
                if (ast.lexeme.token != Token.LeftParenthesis) processEnd(lexeme)
                else State.Defer
            }
            Token.RightParenthesis -> processEnd(lexeme)
            else -> throw CompilationException("Unexpected token", lexeme)
        }
    }

    private fun processOperator(lexeme: Lexeme): State {
        val ancestor = ast.expression
        if (ancestor is BinaryOperator && !ancestor.lexeme.token.highPriority && lexeme.token.highPriority) {
            val binary = BinaryOperator(lexeme, left = ancestor.right)
            ancestor.right = binary
            currentOperator = binary
        } else {
            val binary = BinaryOperator(lexeme, left = ast.expression)
            ast.expression = binary
            currentOperator = binary
        }
        return State.Operator
    }

    private fun processPostfix(lexeme: Lexeme): State {
        val unary = PostfixOperator(lexeme)
        when (currentOperator) {
            is BinaryOperator -> {
                unary.child = (currentOperator as BinaryOperator).right
                (currentOperator as BinaryOperator).right = unary
            }
            is PrefixOperator -> {
                if ((currentOperator as PrefixOperator).lexeme.token == Token.Minus ||
                        (currentOperator as PrefixOperator).lexeme.token == Token.Plus) {
                    unary.child = (currentOperator as PrefixOperator).child
                    (currentOperator as PrefixOperator).child = unary
                } else {
                    throw CompilationException("Illegal operator", lexeme)
                }
            }
            else -> {
                unary.child = ast.expression
                ast.expression = unary
            }
        }
        return State.Postfix
    }

    private fun processEnd(lexeme: Lexeme): State {
        ast.end = lexeme
        ast.linkHierarchy()
        return State.End
    }
}