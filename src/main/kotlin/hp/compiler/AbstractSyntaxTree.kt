package hp.compiler

import kotlin.IllegalStateException

class AbstractSyntaxTree(val tokens: List<Token>) {
    fun parse() = ASTFSM(tokens).run()
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

    override fun toString() = "\n${"  ".repeat(depth)}UnaryNode $state $token child=($child)"
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

    override fun toString() = "\n${"  ".repeat(depth)}BinaryNode $state $token left=($left) right=($right)"
}

class LeafNode(state: ASTState, token: Token) : Node(state, token) {

    override val children = emptyList<Node>()

    override fun replaceChild(child: Node, replacement: Node) = throw IllegalStateException()

    override fun equals(other: Any?): Boolean {
        if (other !is LeafNode) return false
        return other.state == state && other.token == token
    }

    override fun hashCode() = state.hashCode() + token.hashCode()

    override fun toString() = "\n${"  ".repeat(depth)}LeafNode $state $token"
}

enum class ASTState : State {
    Start,
    Number,
    Operator,
    UnaryOperator,
    End
}

class ASTFSM(val tokens: List<Token>) {
    fun run(): Node? {
        var state = ASTState.Start
        return tokens.fold<Token, Node?>(null) { n, it ->
            var node = n
            state = when (state) {
                ASTState.Start -> when (it.type) {
                    TokenType.Number -> {
                        node = LeafNode(ASTState.Number, it)
                        ASTState.Number
                    }
                    TokenType.Plus, TokenType.Minus -> {
                        node = UnaryNode(ASTState.UnaryOperator, it)
                        ASTState.UnaryOperator
                    }
                    else -> ASTState.End
                }
                ASTState.Number -> when (it.type) {
                    TokenType.Plus, TokenType.Minus, TokenType.Times, TokenType.Divide -> {
                        var left = node
                        val operatorNode = BinaryNode(ASTState.Operator, it)
                        if (node is LeafNode) {
                            var p = node.parent
                            var c = node
                            loop@while((p is BinaryNode || p is UnaryNode) && c != null) {
                                if (p.state == ASTState.UnaryOperator || p.token.type == TokenType.Times || p.token.type == TokenType.Divide) {
                                    left = p
                                } else {
                                    p.replaceChild(c, operatorNode)
                                    break@loop
                                }

                                c = p
                                p = p.parent
                            }
                        }
                        operatorNode.left = left
                        node = operatorNode
                        ASTState.Operator
                    }
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
                    else -> throw CompilationException("Failed to parse arithmetic", it)
                }
                ASTState.End -> throw IllegalStateException("Reached end of FSM twice!?")
            }
            node
        }?.topAncestor()
    }
}