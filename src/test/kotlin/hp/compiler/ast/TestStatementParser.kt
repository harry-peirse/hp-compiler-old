package hp.compiler.ast

import hp.compiler.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestStatementParser {
    @Test
    fun `1 + 2`() {
        val lexer = Lexer(" 1 + 2 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[4],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        Literal(lexemes[3])
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `1 + 2 * 3`() {
        val lexer = Lexer(" 1 + 2 * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[6],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        BinaryOperator(lexemes[4],
                                Literal(lexemes[3]),
                                Literal(lexemes[5]))
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `1 div 2 - 3`() {
        val lexer = Lexer(" 1 / 2 - 3 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[6],
                BinaryOperator(lexemes[4],
                        BinaryOperator(lexemes[2],
                                Literal(lexemes[1]),
                                Literal(lexemes[3])),
                        Literal(lexemes[5])
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `123`() {
        val lexer = Lexer(" 123 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[2],
                Literal(lexemes[1])
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `-b`() {
        val lexer = Lexer(" -b ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[3],
                PrefixOperator(lexemes[1],
                        Variable(lexemes[2])
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `1 + 2 * -a - 4 div 5`() {
        val lexer = Lexer(" 1 + 2 * -a - 4 / 5 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[11],
                BinaryOperator(lexemes[7],
                        BinaryOperator(lexemes[2],
                                Literal(lexemes[1]),
                                BinaryOperator(lexemes[4],
                                        Literal(lexemes[3]),
                                        PrefixOperator(lexemes[5],
                                                Variable(lexemes[6])
                                        )
                                )
                        ),
                        BinaryOperator(lexemes[9],
                                Literal(lexemes[8]),
                                Literal(lexemes[10])
                        )
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `(1 + 2) * 3`() {
        val lexer = Lexer(" (1 + 2) * 3 ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[8],
                BinaryOperator(lexemes[6],
                        ScopedExpression(lexemes[1], lexemes[5],
                                BinaryOperator(lexemes[3],
                                        Literal(lexemes[2]),
                                        Literal(lexemes[4])
                                )
                        ),
                        Literal(lexemes[7])
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `1 * -(-1 + 2)`() {
        val lexer = Lexer(" 1 * -(-1 + 2 ) ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[10],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        PrefixOperator(lexemes[3],
                                ScopedExpression(lexemes[4], lexemes[9],
                                        BinaryOperator(lexemes[7],
                                                PrefixOperator(lexemes[5],
                                                        Literal(lexemes[6])
                                                ),
                                                Literal(lexemes[8])
                                        )
                                )
                        )
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `2 * (1 + 2)`() {
        val lexer = Lexer(" 2 * (1 + 2) ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[8],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        ScopedExpression(lexemes[3], lexemes[7],
                                BinaryOperator(lexemes[5],
                                        Literal(lexemes[4]),
                                        Literal(lexemes[6])
                                )
                        )
                )
        )

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `a++`() {
        val lexer = Lexer(" a++ ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[3],
                PostfixOperator(lexemes[2],
                        Variable(lexemes[1])))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `--a`() {
        val lexer = Lexer(" --a ")
        val lexemes = lexer.allLexemes()
        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[3],
                PrefixOperator(lexemes[1],
                        Variable(lexemes[2])))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `-a--`() {
        val lexer = Lexer(" -a-- ")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[4],
                PrefixOperator(lexemes[1],
                        PostfixOperator(lexemes[3],
                                Variable(lexemes[2]))))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `2 + b++`() {
        val lexer = Lexer(" 2 + b ++ ")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[5],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        PostfixOperator(lexemes[4],
                                Variable(lexemes[3]))))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `2 + b++ * 7`() {
        val lexer = Lexer(" 2 + b ++ * 7 ")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[7],
                BinaryOperator(lexemes[2],
                        Literal(lexemes[1]),
                        BinaryOperator(lexemes[5],
                                PostfixOperator(lexemes[4],
                                        Variable(lexemes[3])),
                                Literal(lexemes[6]))))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `1 lessThan 2 lessThan 3`() {
        val lexer = Lexer(" 1 < 2 < 3 ")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[6],
                BinaryOperator(lexemes[4],
                        BinaryOperator(lexemes[2],
                                Literal(lexemes[1]),
                                Literal(lexemes[3])),
                        Literal(lexemes[5])))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `var a = 123`() {
        val lexer = Lexer(" var a = 123 ")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[5],
                Declaration(lexemes[1],
                        Variable(lexemes[2]),
                        ScopedExpression(lexemes[3], lexemes[5],
                                Literal(lexemes[4]))))

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `var a = (1 + b) * c--`() {
        val lexer = Lexer(" var a = (1+b) * c--")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[12],
                Declaration(lexemes[1],
                        Variable(lexemes[2]),
                        ScopedExpression(lexemes[3], lexemes[12],
                                BinaryOperator(lexemes[9],
                                        ScopedExpression(lexemes[4], lexemes[8],
                                                BinaryOperator(lexemes[6],
                                                        Literal(lexemes[5]),
                                                        Variable(lexemes[7]))),
                                        PostfixOperator(lexemes[11],
                                                Variable(lexemes[10]))))))
        expected.linkHierarchy()

        assertEquals(expected, parser.ast)
    }

    @Test
    fun `var a = newline 1 + newline (3 newline ) - ++ newline b newline`() {
        val lexer = Lexer(" var a = \n 1 + \n (3 \n ) - ++ \n b \n")
        val lexemes = lexer.allLexemes()

        val parser = StatementParser(ScopedExpression(lexemes[0]))
        lexemes.subList(1, lexemes.size).forEach { parser.input(it) }

        val expected = ScopedExpression(lexemes[0], lexemes[16],
                Declaration(lexemes[1],
                        Variable(lexemes[2]),
                        ScopedExpression(lexemes[3], lexemes[16],
                                BinaryOperator(lexemes[12],
                                        BinaryOperator(lexemes[6],
                                                Literal(lexemes[5]),
                                                ScopedExpression(lexemes[8], lexemes[11],
                                                        Literal(lexemes[9]))
                                        ),
                                        PrefixOperator(lexemes[13],
                                                Variable(lexemes[15]))))))
        expected.linkHierarchy()

        assertEquals(expected, parser.ast)
    }
}