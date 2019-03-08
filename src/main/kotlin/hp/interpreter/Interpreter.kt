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
    val left = left!!.evaluate().toFloat()
    val right = right!!.evaluate().toFloat()
    return when (lexeme.token) {
        Token.Plus -> (left + right).toString()
        Token.Minus -> (left - right).toString()
        Token.Times -> (left * right).toString()
        Token.Divide -> (left / right).toString()
        Token.Power -> (left.pow(right)).toString()
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

fun AST.evaluate(): String = when (this) {
    is ScopedExpression -> this.evaluate()
    is Variable -> this.evaluate()
    is Literal -> this.evaluate()
    is BinaryOperator -> this.evaluate()
    is PrefixOperator -> this.evaluate()
    is PostfixOperator -> this.evaluate()
    is Declaration -> this.evaluate()
    is Assignment -> this.evaluate()
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