package hp.compiler.ast

import hp.compiler.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestArithemeticParser {
    @Test
    fun `1 + 2`() {
        val lexer = Lexer(" 1 + 2 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[4],
                BinaryArithmeticOperator(lexemes[2],
                        ArithmeticValue(lexemes[1]),
                        ArithmeticValue(lexemes[3])
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `1 + 2 * 3`() {
        val lexer = Lexer(" 1 + 2 * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[6],
                BinaryArithmeticOperator(lexemes[2],
                        ArithmeticValue(lexemes[1]),
                        BinaryArithmeticOperator(lexemes[4],
                                ArithmeticValue(lexemes[3]),
                                ArithmeticValue(lexemes[5]))
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `1 div 2 - 3`() {
        val lexer = Lexer(" 1 / 2 - 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[6],
                BinaryArithmeticOperator(lexemes[4],
                        BinaryArithmeticOperator(lexemes[2],
                                ArithmeticValue(lexemes[1]),
                                ArithmeticValue(lexemes[3])),
                        ArithmeticValue(lexemes[5])
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `123`() {
        val lexer = Lexer(" 123 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[2],
                ArithmeticValue(lexemes[1])
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `-b`() {
        val lexer = Lexer(" -b ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[3],
                UnaryArithmeticOperator(lexemes[1],
                        ArithmeticValue(lexemes[2])
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `1 + 2 * -a - 4 div 5`() {
        val lexer = Lexer(" 1 + 2 * -a - 4 / 5 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[11],
                BinaryArithmeticOperator(lexemes[7],
                        BinaryArithmeticOperator(lexemes[2],
                                ArithmeticValue(lexemes[1]),
                                BinaryArithmeticOperator(lexemes[4],
                                        ArithmeticValue(lexemes[3]),
                                        UnaryArithmeticOperator(lexemes[5],
                                                ArithmeticValue(lexemes[6])
                                        )
                                )
                        ),
                        BinaryArithmeticOperator(lexemes[9],
                                ArithmeticValue(lexemes[8]),
                                ArithmeticValue(lexemes[10])
                        )
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `(1 + 2) * 3`() {
        val lexer = Lexer(" (1 + 2) * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[8],
                BinaryArithmeticOperator(lexemes[6],
                        ScopedArithmeticExpression(lexemes[1], lexemes[5],
                                BinaryArithmeticOperator(lexemes[3],
                                        ArithmeticValue(lexemes[2]),
                                        ArithmeticValue(lexemes[4])
                                )
                        ),
                        ArithmeticValue(lexemes[7])
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }

    @Test
    fun `1 * -(-1 + 2)`() {
        val lexer = Lexer(" 1 * -(-1 + 2 ) ")
        val lexemes = lexer.allLexemes()
        val parser = ArithemeticParser(ScopedArithmeticExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedArithmeticExpression(lexemes[0], lexemes[10],
                BinaryArithmeticOperator(lexemes[2],
                        ArithmeticValue(lexemes[1]),
                        UnaryArithmeticOperator(lexemes[3],
                                ScopedArithmeticExpression(lexemes[4], lexemes[9],
                                        BinaryArithmeticOperator(lexemes[7],
                                                UnaryArithmeticOperator(lexemes[5],
                                                        ArithmeticValue(lexemes[6])
                                                ),
                                                ArithmeticValue(lexemes[8])
                                        )
                                )
                        )
                )
        )

        assertEquals(expected, parser.arithmeticExpression)
    }
}