package hp.compiler

import java.math.BigDecimal

class Interpreter {

    val memory: MutableMap<String, Node> = mutableMapOf()

    fun Node.evaluate(): BigDecimal? = when (this) {
        is LeafNode -> this.evaluate()
        is UnaryNode -> this.evaluate()
        is BinaryNode -> this.evaluate()
    }

    fun LeafNode.evaluate(): BigDecimal? = when (token.type) {
        TokenType.Number -> token.value.toBigDecimal()
        TokenType.Identifier -> memory[token.value]?.evaluate()
                ?: throw CompilationException("Variable was used before it was declared", token)
        else -> throw IllegalStateException()
    }

    fun UnaryNode.evaluate(): BigDecimal? = when (token.type) {
        TokenType.Minus -> {
            val result = child!!.evaluate()
            if (result != null) -result
            else throw CompilationException("Null Reference", token)
        }
        TokenType.Plus -> {
            val result = child!!.evaluate()
            result ?: throw CompilationException("Null Reference", token)
        }
        else -> throw IllegalStateException()
    }

    fun BinaryNode.evaluate(): BigDecimal? = when (token.type) {
        TokenType.Minus -> {
            val l = left!!.evaluate()
            val r = right!!.evaluate()
            if (l != null && r != null) l - r
            else throw CompilationException("Null Reference", token)
        }
        TokenType.Plus -> {
            val l = left!!.evaluate()
            val r = right!!.evaluate()
            if (l != null && r != null) l + r
            else throw CompilationException("Null Reference", token)
        }
        TokenType.Times -> {
            val l = left!!.evaluate()
            val r = right!!.evaluate()
            if (l != null && r != null) l * r
            else throw CompilationException("Null Reference", token)
        }
        TokenType.Divide -> {
            val l = left!!.evaluate()
            val r = right!!.evaluate()
            if (l != null && r != null) l / r
            else throw CompilationException("Null Reference", token)
        }
        TokenType.Assign -> {
            memory[left!!.token.value] = right!!
            null
        }
        else -> throw IllegalStateException()
    }

    fun run(vararg raw: String): String = raw
            .map { AbstractSyntaxTree(Lexer(it).allTokens()).parse()?.evaluate() }
            .filterNotNull()
            .joinToString("\n")
}