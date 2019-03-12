package hp.interpreter
import hp.compiler.*
import hp.compiler.ast.StatementParser
import hp.compiler.lexer.Lexer
import java.io.File
import java.sql.Statement
import kotlin.math.pow

val memory = mutableMapOf<String, String>()

fun ScopedExpression.evaluate(): String = expression?.evaluate() ?: ""
fun Literal.evaluate(): String = lexeme.value!!
fun Variable.evaluate(): String = memory[this.lexeme.value] ?: throw CompilationException("Can't use variable before declaring it", lexeme)
fun PrefixOperator.evaluate(): String = when (lexeme.token) {
    Token.Increment -> {
        memory[child!!.lexeme.value!!] = ((memory[child!!.lexeme.value] ?: throw CompilationException("Can't use variable before declaring it", lexeme)).toFloat() + 1).toString()
        memory[child!!.lexeme.value]!!
    }
    Token.Decrement -> {
        memory[child!!.lexeme.value!!] = ((memory[child!!.lexeme.value] ?: throw CompilationException("Can't use variable before declaring it", lexeme)).toFloat() - 1).toString()
        memory[child!!.lexeme.value!!]!!
    }
    Token.Plus -> (+child!!.evaluate().toFloat()).toString()
    Token.Minus -> (-child!!.evaluate().toFloat()).toString()
    else -> throw IllegalStateException()
}

fun PostfixOperator.evaluate(): String = when (lexeme.token) {
    Token.Increment -> {
        val result = memory[child!!.lexeme.value]!!
        memory[child!!.lexeme.value!!] = ((memory[child!!.lexeme.value] ?: throw CompilationException("Can't use variable before declaring it", lexeme)).toFloat() + 1).toString()
        result
    }
    Token.Decrement -> {
        val result = memory[child!!.lexeme.value]!!
        memory[child!!.lexeme.value!!] = ((memory[child!!.lexeme.value] ?: throw CompilationException("Can't use variable before declaring it", lexeme)).toFloat() - 1).toString()
        result
    }
    else -> throw IllegalStateException()
}

fun BinaryOperator.evaluate(): String {
    var left = this.left!!.evaluate()
    var right = this.right!!.evaluate()
    return when (lexeme.token) {
        Token.Plus -> (left.toFloat() + right.toFloat()).toString()
        Token.Minus -> (left.toFloat() - right.toFloat()).toString()
        Token.Times -> (left.toFloat() * right.toFloat()).toString()
        Token.Divide -> (left.toFloat() / right.toFloat()).toString()
//        Token.Power -> (left.pow(right)).toString()
        Token.BitwiseAnd -> {
            val length = if(left.length > right.length) left.length else right.length
            left = left.padStart(length, '0')
            right = right.padStart(length, '0')
            var result = ""

            for(i in 0 until length) {
                result += if(left[i] == '1' && right[i] == '1') '1' else '0'
            }

            result
        }
        Token.BitwiseOr -> {
            val length = if(left.length > right.length) left.length else right.length
            left = left.padStart(length, '0')
            right = right.padStart(length, '0')
            var result = ""

            for(i in 0 until length) {
                result += if(left[i] == '1' || right[i] == '1') '1' else '0'
            }

            result
        }
        Token.LeftShift -> {
            val shiftAmount = right.toInt()

            var result = left.padEnd(left.length + shiftAmount, '0')
            result = result.substring(shiftAmount)

            result
        }
        Token.RightShift -> {
            val shiftAmount = right.toInt()

            var result = left.padStart(left.length + shiftAmount, '0')
            result = result.substring(0, left.length)

            result
        }
        else -> throw IllegalStateException()
    }
}

fun Declaration.evaluate(): String {
    memory[variable!!.lexeme.value!!] = expression!!.evaluate()
    return ""
}

fun Assignment.evaluate(): String {
    if(memory.containsKey(variable!!.lexeme.value)) {
        memory[variable!!.lexeme.value!!] = expression!!.evaluate()
        return ""
    } else throw CompilationException("Can't use variable before declaring it", lexeme)
}

fun Scope.evaluate(): String =
    children.joinToString("\n") { it.evaluate() }

fun AST.evaluate(): String = when (this) {
    is ScopedExpression -> this.evaluate()
    is Variable -> this.evaluate()
    is Literal -> this.evaluate()
    is BinaryOperator -> this.evaluate()
    is PrefixOperator -> this.evaluate()
    is PostfixOperator -> this.evaluate()
    is Declaration -> this.evaluate()
    is Assignment -> this.evaluate()
    is Scope -> this.evaluate()
}

fun main(args: Array<String>) {
    Interpreter().run(args)
}

class Interpreter {
    fun run(args: Array<String>) {
        val source = args
                .map { File(it) }
                .filter { it.exists() && it.isFile }
                .map { it.readText() }
                .joinToString("\n\n")

        val compiler = Compiler(source)
        val expressions = compiler.compile()

        expressions.forEach { println(it.evaluate()) }
    }

    var parser: StatementParser? = null

    fun interpret(source: String) {
        val lexer = Lexer(source)
        val lexemes = lexer.allLexemes()
        if (lexemes.isNotEmpty()) {
            var i = 0
            var lexeme = lexemes[i]
            while (lexeme.token != Token.EndOfInput) {
                if(parser == null) {
                    parser = StatementParser(ScopedExpression(lexeme))
                }
                while (!parser!!.finished) {
                    lexeme = lexemes[++i]
                    if(lexeme.token == Token.EndOfInput) {
                        lexeme = Lexeme(Token.NewLine, lexeme.position)
                        parser!!.input(lexeme)
                        if(parser!!.finished) {
                            val result = parser!!.ast.evaluate()
                            if(result.isNotBlank()) {
                                println(result)
                            }
                            parser = null
                        }
                        return
                    }
                    parser!!.input(lexeme)
                }
                val result = parser!!.ast.evaluate()
                if(result.isNotBlank()) {
                    println(result)
                }
                parser = null
            }
        }
    }
}