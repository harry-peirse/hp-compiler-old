package hp.compiler.ast

import hp.compiler.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestExpressionParser {
    @Test
    fun `1 + 2`() {
        val lexer = Lexer(" 1 + 2 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[4],
                BinaryOperator(lexemes[2],
                        Value(lexemes[1]),
                        Value(lexemes[3])
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `1 + 2 * 3`() {
        val lexer = Lexer(" 1 + 2 * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[6],
                BinaryOperator(lexemes[2],
                        Value(lexemes[1]),
                        BinaryOperator(lexemes[4],
                                Value(lexemes[3]),
                                Value(lexemes[5]))
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `1 div 2 - 3`() {
        val lexer = Lexer(" 1 / 2 - 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[6],
                BinaryOperator(lexemes[4],
                        BinaryOperator(lexemes[2],
                                Value(lexemes[1]),
                                Value(lexemes[3])),
                        Value(lexemes[5])
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `123`() {
        val lexer = Lexer(" 123 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[2],
                Value(lexemes[1])
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `-b`() {
        val lexer = Lexer(" -b ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[3],
                PrefixOperator(lexemes[1],
                        Value(lexemes[2])
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `1 + 2 * -a - 4 div 5`() {
        val lexer = Lexer(" 1 + 2 * -a - 4 / 5 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[11],
                BinaryOperator(lexemes[7],
                        BinaryOperator(lexemes[2],
                                Value(lexemes[1]),
                                BinaryOperator(lexemes[4],
                                        Value(lexemes[3]),
                                        PrefixOperator(lexemes[5],
                                                Value(lexemes[6])
                                        )
                                )
                        ),
                        BinaryOperator(lexemes[9],
                                Value(lexemes[8]),
                                Value(lexemes[10])
                        )
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `(1 + 2) * 3`() {
        val lexer = Lexer(" (1 + 2) * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[8],
                BinaryOperator(lexemes[6],
                        ScopedExpression(lexemes[1], lexemes[5],
                                BinaryOperator(lexemes[3],
                                        Value(lexemes[2]),
                                        Value(lexemes[4])
                                )
                        ),
                        Value(lexemes[7])
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `1 * -(-1 + 2)`() {
        val lexer = Lexer(" 1 * -(-1 + 2 ) ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[10],
                BinaryOperator(lexemes[2],
                        Value(lexemes[1]),
                        PrefixOperator(lexemes[3],
                                ScopedExpression(lexemes[4], lexemes[9],
                                        BinaryOperator(lexemes[7],
                                                PrefixOperator(lexemes[5],
                                                        Value(lexemes[6])
                                                ),
                                                Value(lexemes[8])
                                        )
                                )
                        )
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }

    @Test
    fun `2 * (1 + 2)`() {
        val lexer = Lexer(" 2 * (1 + 2) ")
        val lexemes = lexer.allLexemes()
        val parser = ExpressionParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[8],
                BinaryOperator(lexemes[2],
                        Value(lexemes[1]),
                        ScopedExpression(lexemes[3], lexemes[7],
                                BinaryOperator(lexemes[5],
                                        Value(lexemes[4]),
                                        Value(lexemes[6])
                                )
                        )
                )
        )

        assertEquals(expected, parser.scopedExpression)
    }
}