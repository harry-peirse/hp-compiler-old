package hp.compiler

import kotlin.IllegalStateException

const val DEBUG_LOGGING = false

class AbstractSyntaxTree(val tokens: List<Token>) {
    val astFsm = AbstractSyntaxTreeFiniteStateMachine(tokens)
    val index: Int
        get() = astFsm.index
    fun parse() = astFsm.run()
}

sealed class Node(val state: ASTState, val token: Token) {
    abstract val children: List<Node>

    var parent: Node? = null
    val depth: Int
        get() {
            var d = 0
            var p = parent
            while (p != null) {
                p = p.parent
                d++
            }
            return d
        }

    fun topAncestor(): Node {
        var p = this
        while (p.parent != null) {
            p = p.parent!!
        }
        return p
    }

    abstract fun replaceChild(child: Node, replacement: Node)

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract override fun toString(): String
}

class UnaryNode(state: ASTState, token: Token) : Node(state, token) {
    var child: Node? = null
        set(v) {
            v?.parent = this
            field = v
        }

    override val children get() = listOfNotNull(child)

    override fun replaceChild(child: Node, replacement: Node) {
        if(this.child == child) {
            this.child = replacement
        } else throw IllegalStateException()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UnaryNode) return false
        if (other.state != state || other.token != token) return false
        return other.child == child
    }

    override fun hashCode() = state.hashCode() + token.hashCode() + child.hashCode()

    override fun toString() = "UnaryNode  $state $token \n   ${"     ".repeat(depth+1)}$child"
}

class BinaryNode(state: ASTState, token: Token) : Node(state, token) {
    var left: Node? = null
        set(v) {
            v?.parent = this
            field = v
        }
    var right: Node? = null
        set(v) {
            v?.parent = this
            field = v
        }

    override val children get() = listOfNotNull(left, right)

    override fun replaceChild(child: Node, replacement: Node) {
        if (left == child) left = replacement
        else if (right == child) right = replacement
        else throw IllegalStateException("Tried to replace child that didn't exist")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BinaryNode) return false
        if (other.state != state || other.token != token) return false
        return other.left == left && other.right == right
    }

    override fun hashCode() = state.hashCode() + token.hashCode() + left.hashCode() + right.hashCode()

    override fun toString() = "BinaryNode $state $token \n${"     ".repeat(depth+1)}L: $left\n${"     ".repeat(depth+1)}R: $right"
}

class LeafNode(state: ASTState, token: Token) : Node(state, token) {

    override val children = emptyList<Node>()

    override fun replaceChild(child: Node, replacement: Node) = throw IllegalStateException()

    override fun equals(other: Any?): Boolean {
        if (other !is LeafNode) return false
        return other.state == state && other.token == token
    }

    override fun hashCode() = state.hashCode() + token.hashCode()

    override fun toString() = "LeafNode   $state $token"
}

enum class ASTState : State {
    Start,
    Number,
    Identifier,
    Operator,
    UnaryOperator,
    RightParenthesis,
    End
}

class AbstractSyntaxTreeFiniteStateMachine(val tokens: List<Token>) {

    var index: Int = 0

    fun run(): Node? {
        var state = ASTState.Start
        var node: Node? = null

        if(DEBUG_LOGGING) println(">>> FSM")

        while(index < tokens.size) {
            val it = tokens[index++]

            if(DEBUG_LOGGING) println("$state:   ( $index ) -> $it")

            state = when (state) {
                ASTState.Start -> when (it.type) {
                    TokenType.Number -> {
                        node = LeafNode(ASTState.Number, it)
                        ASTState.Number
                    }
                    TokenType.Identifier -> {
                        node = LeafNode(ASTState.Identifier, it)
                        ASTState.Identifier
                    }
                    TokenType.Plus, TokenType.Minus -> {
                        node = UnaryNode(ASTState.UnaryOperator, it)
                        ASTState.UnaryOperator
                    }
                    TokenType.LeftParenthesis -> {
                        val ast = AbstractSyntaxTree(tokens.subList(index, tokens.size))
                        val subTree: Node? = ast.parse()
                        if(subTree != null) {
                            node = subTree
                            index += ast.index
                            ASTState.RightParenthesis
                        } else {
                            index++
                            ASTState.Start
                        }
                    }
                    else -> ASTState.End
                }

                ASTState.Number -> when (it.type) {
                    TokenType.Plus, TokenType.Minus, TokenType.Times, TokenType.Divide -> {
                        node = insertOperationAccordingToOrderOfOperations(node, it)
                        ASTState.Operator
                    }
                    TokenType.RightParenthesis -> ASTState.End
                    TokenType.EndOfInput -> ASTState.End
                    else -> throw CompilationException("Failed to parse arithmetic", it)
                }

                ASTState.Identifier -> when(it.type) {
                    TokenType.Assign, TokenType.Plus, TokenType.Minus, TokenType.Times, TokenType.Divide -> {
                        node = insertOperationAccordingToOrderOfOperations(node, it)
                        ASTState.Operator
                    }
                    TokenType.RightParenthesis -> ASTState.End
                    TokenType.EndOfInput -> ASTState.End
                    else -> throw CompilationException("Failed to parse arithmetic", it)
                }

                ASTState.UnaryOperator -> when (it.type) {
                    TokenType.Number -> {
                        if (node is UnaryNode) {
                            node.child = LeafNode(ASTState.Number, it)
                            node = node.child
                        } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        ASTState.Number
                    }
                    TokenType.Identifier -> {
                        if (node is UnaryNode) {
                            node.child = LeafNode(ASTState.Identifier, it)
                            node = node.child
                        } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        ASTState.Identifier
                    }
                    TokenType.LeftParenthesis -> {
                        val ast = AbstractSyntaxTree(tokens.subList(index, tokens.size))
                        val subTree: Node? = ast.parse()
                        if(subTree != null) {
                            if (node is UnaryNode) {
                                node.child = subTree
                                node = subTree
                                index += ast.index
                                ASTState.RightParenthesis
                            } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        } else {
                            index++
                            ASTState.UnaryOperator
                        }
                    }
                    else -> throw CompilationException("Failed to parse arithmetic", it)
                }

                ASTState.Operator -> when (it.type) {
                    TokenType.Plus, TokenType.Minus -> {
                        if (node is BinaryNode) {
                            node.right = UnaryNode(ASTState.UnaryOperator, it)
                            node = node.right
                        } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        ASTState.UnaryOperator
                    }
                    TokenType.Number -> {
                        if (node is BinaryNode) {
                            node.right = LeafNode(ASTState.Number, it)
                            node = node.right
                        } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        ASTState.Number
                    }
                    TokenType.Identifier -> {
                        if (node is BinaryNode) {
                            node.right = LeafNode(ASTState.Identifier, it)
                            node = node.right
                        } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        ASTState.Identifier
                    }
                    TokenType.LeftParenthesis -> {
                        val ast = AbstractSyntaxTree(tokens.subList(index, tokens.size))
                        val subTree: Node? = ast.parse()
                        if(subTree != null) {
                            if (node is BinaryNode) {
                                node.right = subTree
                                node = subTree
                                index += ast.index
                                ASTState.RightParenthesis
                            } else throw IllegalStateException("Expected previous UnaryOperator to be captured in UnaryNode")
                        } else {
                            index++
                            ASTState.Operator
                        }
                    }
                    else -> throw CompilationException("Failed to parse arithmetic", it)
                }

                ASTState.RightParenthesis -> when(it.type) {
                    TokenType.Plus, TokenType.Minus, TokenType.Times, TokenType.Divide -> {
                        node = insertOperationAccordingToOrderOfOperations(node, it)
                        ASTState.Operator
                    }
                    TokenType.EndOfInput -> ASTState.End
                    else -> throw CompilationException("Expected end of expression or operator", it)
                }

                ASTState.End -> throw IllegalStateException("Reached end of FSM twice!?")
            }

            if(DEBUG_LOGGING) {
                println()
                println("FULL TREE:")
                println(node?.topAncestor())
                println()
            }

            if(state == ASTState.End) break
        }
        if(DEBUG_LOGGING)println("<<< FSM")
        return node?.topAncestor()
    }

    private fun insertOperationAccordingToOrderOfOperations(n: Node?, it: Token): Node {
        var node = n
        val operatorNode = BinaryNode(ASTState.Operator, it)
        if (node != null) {
            var currentParent = node.parent
            var currentNode = node
            loop@ while ((currentParent is BinaryNode || currentParent is UnaryNode) && currentNode != null) {
                if (currentParent.state == ASTState.UnaryOperator || currentParent.token.type.orderOfOperationsPriority <= it.type.orderOfOperationsPriority) {
                    currentNode = currentParent
                    currentParent = currentNode.parent
                } else {
                    currentParent.replaceChild(currentNode, operatorNode)
                    break@loop
                }
            }
            node = currentNode
        }
        operatorNode.left = node

        return operatorNode
    }
}