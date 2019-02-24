package hp.compiler

import java.math.BigDecimal

fun Node.evaluate(): BigDecimal = when (this) {
    is LeafNode -> this.evaluate()
    is UnaryNode -> this.evaluate()
    is BinaryNode -> this.evaluate()
}

fun LeafNode.evaluate(): BigDecimal = token.value.toBigDecimal()

fun UnaryNode.evaluate(): BigDecimal = when (token.type) {
    TokenType.Minus -> -child!!.evaluate()
    TokenType.Plus -> child!!.evaluate()
    else -> throw IllegalStateException()
}

fun BinaryNode.evaluate(): BigDecimal = when (token.type) {
    TokenType.Minus -> left!!.evaluate() - right!!.evaluate()
    TokenType.Plus -> left!!.evaluate() + right!!.evaluate()
    TokenType.Times -> left!!.evaluate() * right!!.evaluate()
    TokenType.Divide -> left!!.evaluate() / right!!.evaluate()
    else -> throw IllegalStateException()
}

class Interpreter(val raw: String) {
    fun run() = AbstractSyntaxTree(Lexer(raw).allTokens()).parse()?.evaluate().toString()
}