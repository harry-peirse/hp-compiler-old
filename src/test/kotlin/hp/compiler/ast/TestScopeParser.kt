package hp.compiler.ast

import hp.compiler.*
import hp.compiler.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals

class TestScopeParser {
    @Test
    fun `1 + 2 semicolon 3 + 4`() {
        val lexer = Lexer(" 1 + 2; 3 + 4 ")
        val lexemes = lexer.allLexemes()
        val parser = ScopeParser(Scope(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = Scope(lexemes[0], lexemes[8], mutableListOf(
                ScopedExpression(lexemes[1], lexemes[4],
                        BinaryOperator(lexemes[2],
                                Literal(lexemes[1]),
                                Literal(lexemes[3]))),
                ScopedExpression(lexemes[5], lexemes[8],
                        BinaryOperator(lexemes[6],
                                Literal(lexemes[5]),
                                Literal(lexemes[7]))))
        )
        expected.linkHierarchy()

        assertEquals(expected, parser.ast)
    }

    @Test
    fun ` { 1 + 2 newline { 3 + 4 } } 5 div 6 `() {
        val lexer = Lexer(" { 1 + 2 \n {3 + 4} } 5 / 6 ")
        val lexemes = lexer.allLexemes()
        val parser = ScopeParser(Scope(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = Scope(lexemes[0], lexemes[15], mutableListOf(
                Scope(lexemes[1], lexemes[11], mutableListOf(
                        ScopedExpression(lexemes[2], lexemes[5],
                                BinaryOperator(lexemes[3],
                                        Literal(lexemes[2]),
                                        Literal(lexemes[4]))),
                        Scope(lexemes[6], lexemes[10], mutableListOf(
                                ScopedExpression(lexemes[7], lexemes[10],
                                        BinaryOperator(lexemes[8],
                                                Literal(lexemes[7]),
                                                Literal(lexemes[9])))
                        ))
                )),
                ScopedExpression(lexemes[12], lexemes[15],
                        BinaryOperator(lexemes[13],
                                Literal(lexemes[12]),
                                Literal(lexemes[14])))))
        expected.linkHierarchy()

        assertEquals(expected, parser.ast)
    }
}