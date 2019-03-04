//package hp.compiler
//
//import kotlin.IllegalStateException
//
//const val LOGGING_INDENT = "    "
//const val DEBUG_LOGGING = true
//
//class AST_FSM(tokens: List<Lexeme>) {
//    val astFsm = AbstractSyntaxTreeFiniteStateMachine(tokens)
//    val index: Int
//        get() = astFsm.index
//
//    fun parse() = astFsm.run()
//}
//
//sealed class AST(val token: Lexeme) {
//    var parent: AST? = null
//
//    val depth: Int
//        get() {
//            var d = 0
//            var p = parent
//            while (p != null) {
//                p = p.parent
//                d++
//            }
//            return d
//        }
//    val childDepthString: String
//        get() = LOGGING_INDENT.repeat(depth + 1)
//
//    val ancestor: AST
//        get() {
//            var p = this
//            while (p.parent != null) {
//                p = p.parent!!
//            }
//            return p
//        }
//
//    fun firstAncestor(predicate: (AST) -> Boolean): AST {
//        var p: AST? = this
//        while (p != null) {
//            if (predicate(p)) {
//                return p
//            } else {
//                p = p.parent
//            }
//        }
//        throw NoSuchElementException()
//    }
//
//    override fun equals(other: Any?) = other is AST && other.token == token
//    override fun hashCode() = token.hashCode()
//    override fun toString() = "AST($token)"
//}
//
//sealed class ASTParent(token: Lexeme) : AST(token) {
//    abstract fun replaceChild(child: AST, replacement: AST)
//}
//
//interface Scoped
//
//class UnaryOperator(token: Lexeme) : ASTParent(token) {
//    var child: AST? = null
//        set(v) {
//            v?.parent = this
//            field = v
//        }
//
//    override fun replaceChild(child: AST, replacement: AST) {
//        if (this.child == child) {
//            this.child = replacement
//        } else throw IllegalStateException()
//    }
//
//    override fun equals(other: Any?) = other is UnaryOperator && other.token == token && other.child == child
//    override fun hashCode() = token.hashCode() + child.hashCode()
//    override fun toString() = "UnaryOperator($token \n$childDepthString    $child)"
//}
//
//class BinaryOperator(token: Lexeme) : ASTParent(token) {
//    var left: AST? = null
//        set(v) {
//            v?.parent = this
//            field = v
//        }
//    var right: AST? = null
//        set(v) {
//            v?.parent = this
//            field = v
//        }
//
//    override fun replaceChild(child: AST, replacement: AST) {
//        if (left == child) left = replacement
//        else if (right == child) right = replacement
//        else throw IllegalStateException("Tried to replace child that didn't exist")
//    }
//
//    override fun equals(other: Any?) = other is BinaryOperator && other.token == token && other.left == left && other.right == right
//    override fun hashCode() = token.hashCode() + left.hashCode() + right.hashCode()
//    override fun toString() = "BinaryOperator($token \n$childDepthString L: $left \n$childDepthString R: $right)"
//}
//
//class ScopedExpression(token: Lexeme) : ASTParent(token), Scoped {
//    var endLexeme: Lexeme? = null
//
//    var child: AST? = null
//        set(v) {
//            v?.parent = this
//            field = v
//        }
//
//    override fun replaceChild(child: AST, replacement: AST) {
//        if (this.child == child) {
//            this.child = replacement
//        } else throw IllegalStateException()
//    }
//
//    override fun equals(other: Any?) = other is ScopedExpression && other.token == token && other.endLexeme == endLexeme && other.child == child
//    override fun hashCode() = token.hashCode() + endLexeme.hashCode() + child.hashCode()
//    override fun toString() = "ScopedExpression($token to $endLexeme \n$childDepthString    $child)"
//}
//
//class Scope(token: Lexeme) : ASTParent(token), Scoped {
//
//    var endLexeme: Lexeme? = null
//
//    private val _statements = mutableListOf<AST>()
//    val statements: List<AST> = _statements
//
//    operator fun plusAssign(statement: AST) {
//        _statements.add(statement)
//        statement.parent = this
//    }
//
//    override fun replaceChild(child: AST, replacement: AST) {
//        if (DEBUG_LOGGING) println("SCOPE $this replaceChild: $child with $replacement")
//        if (_statements.contains(child)) {
//            val index = _statements.indexOf(child)
//            _statements.remove(child)
//            _statements.add(index, replacement)
//            replacement.parent = this
//        }
//    }
//
//    override fun equals(other: Any?) = other is Scope && other.token == token && other.endLexeme == endLexeme && other.statements == statements
//    override fun hashCode() = token.hashCode() + endLexeme.hashCode() + statements.hashCode()
//    override fun toString() = "Scope($token to $endLexeme${if (statements.isNotEmpty()) " \n$childDepthString" else ""}${statements.joinToString("\n$childDepthString")})"
//}
//
//class Literal(token: Lexeme) : AST(token) {
//    override fun equals(other: Any?) = other is Literal && other.token == token
//    override fun hashCode() = token.hashCode()
//    override fun toString() = "Literal($token)"
//}
//
//class Variable(token: Lexeme) : AST(token) {
//    override fun equals(other: Any?) = other is Variable && other.token == token
//    override fun hashCode() = token.hashCode()
//    override fun toString() = "Variable($token)"
//}
//
//enum class ASTState : State {
//    Start, End,
//    StartScope, EndScope,
//    StartStatement, EndStatement,
//    StartParenthesis, EndParenthesis,
//    Operator, UnaryOperator,
//    Number,
//    Identifier
//}
//
//class AbstractSyntaxTreeFiniteStateMachine(val tokens: List<Lexeme>) {
//
//    var index: Int = 0
//
//    fun run(): AST? {
//        var state = ASTState.Start
//        var ast: AST? = null
//        var scope = Scope(Lexeme(Token.LeftBrace))
//
//        if (DEBUG_LOGGING) println(" >>> FSM >>> ")
//
//        while (state != ASTState.End) {
//            val it = tokens[index]
//
//            if (DEBUG_LOGGING) println("$state: $index -> $it")
//
//            state = when (state) {
//                ASTState.StartParenthesis -> {
//                    val scopedExpression = ScopedExpression(it)
//                    when (ast) {
//                        null -> scope += scopedExpression
//                        is UnaryOperator -> ast.child = scopedExpression
//                        is BinaryOperator -> ast.right = scopedExpression
//                        else -> throw IllegalStateException()
//                    }
//                    ast = scopedExpression
//                    index++
//                    if (index < tokens.size) {
//                        val token = tokens[index]
//                        index++
//                        when (token.token) {
//                            Token.Number -> {
//                                val node = Literal(token)
//                                scopedExpression.child = node
//                                ast = node
//                                ASTState.Number
//                            }
//                            Token.Identifier -> {
//                                val node = Variable(token)
//                                scopedExpression.child = node
//                                ast = node
//                                ASTState.Identifier
//                            }
//                            Token.Minus, Token.Plus -> {
//                                val node = UnaryOperator(token)
//                                scopedExpression.child = node
//                                ast = node
//                                ASTState.UnaryOperator
//                            }
//                            Token.LeftParenthesis -> ASTState.StartParenthesis
//                            else -> throw CompilationException("Unexpected character", token)
//                        }
//                    } else throw CompilationException("Unexpected end of input", it)
//                }
//
//                ASTState.StartScope -> {
//                    val newScope = Scope(it)
//                    scope += newScope
//                    scope = newScope
//                    ASTState.StartStatement
//                }
//
//                ASTState.EndScope -> {
//                    scope = scope.parent as Scope
//                    ASTState.StartStatement
//                }
//
//                ASTState.EndStatement -> {
//                    ast = null
//                    ASTState.StartStatement
//                }
//
//                ASTState.Start, ASTState.StartStatement -> {
//                    when (it.token) {
//                        Token.NewLine -> {
//                            index++
//                            ASTState.StartStatement
//                        }
//                        Token.Number -> {
//                            index++
//                            ast = Literal(it)
//                            scope += ast
//                            ASTState.Number
//                        }
//                        Token.Identifier -> {
//                            index++
//                            ast = Variable(it)
//                            scope += ast
//                            ASTState.Identifier
//                        }
//                        Token.Plus, Token.Minus -> {
//                            index++
//                            ast = UnaryOperator(it)
//                            scope += ast
//                            ASTState.UnaryOperator
//                        }
//                        Token.LeftParenthesis -> {
//                            ASTState.StartParenthesis
//                        }
//                        Token.RightBrace -> {
//                            index++
//                            scope.endLexeme = it
//                            scope = scope.parent as Scope
//                            ASTState.EndStatement
//                        }
//                        else -> ASTState.End
//                    }
//                }
//
//                ASTState.Number, ASTState.Identifier, ASTState.EndParenthesis -> {
//                    when (it.token) {
//                        Token.Plus, Token.Minus, Token.Times, Token.Divide -> {
//                            index++
//                            ast = insertOperatorAccordingToOrderOfOperations(ast, it)
//                            ASTState.Operator
//                        }
//                        Token.Assign -> {
//                            if (state == ASTState.Identifier) {
//                                index++
//                                ast = insertOperatorAccordingToOrderOfOperations(ast, it)
//                                ASTState.Operator
//                            } else throw CompilationException("Can't assign expression to a literal", it)
//                        }
//                        Token.NewLine -> {
//                            index++
//                            ASTState.EndStatement
//                        }
//                        Token.RightParenthesis -> {
//                            val scopedExpression = (ast?.firstAncestor { it is ScopedExpression } as ScopedExpression)
//                            scopedExpression.endLexeme = it
//                            ast = scopedExpression
//                            index++
//                            if (index < tokens.size) {
//                                val token = tokens[index]
//                                when (token.token) {
//                                    Token.Plus, Token.Minus, Token.Times, Token.Divide -> {
//                                        index++
//                                        ast = insertOperatorAccordingToOrderOfOperations(ast, token)
//                                        ASTState.Operator
//                                    }
//                                    Token.NewLine -> {
//                                        index++
//                                        ASTState.EndStatement
//                                    }
//                                    Token.EndOfInput -> ASTState.End
//                                    else -> throw CompilationException("Invalid token", token)
//                                }
//                            } else throw CompilationException("Unexpected end of input", it)
//                        }
//
//                        Token.EndOfInput -> ASTState.End
//                        else -> throw CompilationException("Failed to parse arithmetic", it)
//                    }
//                }
//
//                ASTState.UnaryOperator -> {
//                    when (it.token) {
//                        Token.Number -> {
//                            index++
//                            if (ast is UnaryOperator) {
//                                ast.child = Literal(it)
//                                ast = ast.child
//                            } else throw IllegalStateException()
//                            ASTState.Number
//                        }
//                        Token.Identifier -> {
//                            index++
//                            if (ast is UnaryOperator) {
//                                ast.child = Variable(it)
//                                ast = ast.child
//                            } else throw IllegalStateException()
//                            ASTState.Identifier
//                        }
//                        Token.LeftParenthesis -> {
//                            ASTState.StartParenthesis
//                        }
//                        else -> throw CompilationException("Failed to parse arithmetic", it)
//                    }
//                }
//
//                ASTState.Operator -> {
//                    when (it.token) {
//                        Token.Plus, Token.Minus, Token.Number, Token.Identifier -> {
//                            index++
//                            if (ast is BinaryOperator) {
//                                ast.right = when (it.token) {
//                                    Token.Number -> Literal(it)
//                                    Token.Identifier -> Variable(it)
//                                    else -> UnaryOperator(it)
//                                }
//                                ast = ast.right
//                            } else throw IllegalStateException()
//                            when (it.token) {
//                                Token.Number -> ASTState.Number
//                                Token.Identifier -> ASTState.Identifier
//                                else -> ASTState.UnaryOperator
//                            }
//                        }
//                        Token.LeftParenthesis -> {
//                            ASTState.StartParenthesis
//                        }
//                        else -> throw CompilationException("Failed to parse arithmetic", it)
//                    }
//                }
//
//                ASTState.End -> {
//                    throw IllegalStateException()
//                }
//            }
//
//            if (state == ASTState.End && scope.endLexeme == null) {
//                scope.endLexeme = Lexeme(Token.RightBrace, it.position)
//            }
//
//            if (DEBUG_LOGGING) {
//                println()
//                println("$state FULL TREE:")
//                println(ast?.ancestor)
//                println()
//            }
//        }
//
//        if (DEBUG_LOGGING) println(" <<< FSM <<< ")
//        return ast?.ancestor
//    }
//
//    private fun insertOperatorAccordingToOrderOfOperations(n: AST?, it: Lexeme): AST {
//        var node = n
//        val operatorNode = BinaryOperator(it)
//        if (node != null) {
//            var currentParent = node.parent
//            var currentNode: AST = node
//
//            while (currentParent is ASTParent) {
//                if (currentParent !is Scoped && (currentParent is UnaryOperator || currentParent.token.token.highPriority || !it.token.highPriority)) {
//                    currentNode = currentParent
//                    currentParent = currentNode.parent
//                } else {
//                    currentParent.replaceChild(currentNode, operatorNode)
//                    break
//                }
//            }
//
//            node = currentNode
//        }
//        operatorNode.left = node
//
//        return operatorNode
//    }
//}