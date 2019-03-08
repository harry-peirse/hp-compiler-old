package hp.compiler

import hp.compiler.ast.StatementParser
import hp.compiler.lexer.Lexer

class Compiler(val source: String) {
    fun compile(): List<ScopedExpression> {
        val lexer = Lexer(source)
        val lexemes = lexer.allLexemes()
        if (lexemes.isNotEmpty()) {
            val statements = mutableListOf<ScopedExpression>()
            var i = 0
            var lexeme = lexemes[i]
            while (lexeme.token != Token.EndOfInput) {
                val parser = StatementParser(ScopedExpression(lexeme))
                while (!parser.finished) {
                    lexeme = lexemes[++i]
                    parser.input(lexeme)
                }
                println("---")
                statements.add(parser.ast)
            }
            return statements
        }
        return emptyList()
    }
}