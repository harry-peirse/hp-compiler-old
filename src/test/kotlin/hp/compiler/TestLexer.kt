package hp.compiler

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import hp.compiler.Lexer.Companion.isOperator
import hp.compiler.Lexer.Companion.isParenthesis
import kotlin.test.assertEquals

class TestLexer {

    @Test(expected = CompilationException::class)
    fun compilation_exception() {
        Lexer("@").nextLexeme()
    }

    @Test
    fun regex() {
        assertTrue('+'.isOperator())
        assertTrue('-'.isOperator())
        assertTrue('*'.isOperator())
        assertTrue('/'.isOperator())
        assertTrue('='.isOperator())
        assertTrue('<'.isOperator())
        assertTrue('>'.isOperator())

        assertFalse('1'.isOperator())
        assertFalse('a'.isOperator())
        assertFalse('('.isOperator())
        assertFalse(')'.isOperator())

        assertTrue('('.isParenthesis())
        assertTrue(')'.isParenthesis())

        assertFalse('1'.isParenthesis())
        assertFalse('a'.isParenthesis())
        assertFalse('+'.isParenthesis())
        assertFalse('-'.isParenthesis())
        assertFalse('*'.isParenthesis())
        assertFalse('/'.isParenthesis())
        assertFalse('='.isParenthesis())
        assertFalse('<'.isParenthesis())
        assertFalse('>'.isParenthesis())
    }

    @Test
    fun lex_comparison_operators() {
        assertEquals(Lexeme(Token.LessThan, Position(1, 1)), Lexer("<").nextLexeme())
        assertEquals(Lexeme(Token.LessThanOrEqualTo, Position(1, 1)), Lexer("<=").nextLexeme())
        assertEquals(Lexeme(Token.GreaterThan, Position(1, 1)), Lexer(">").nextLexeme())
        assertEquals(Lexeme(Token.GreaterThanOrEqualTo, Position(1, 1)), Lexer(">=").nextLexeme())
        assertEquals(Lexeme(Token.EqualTo, Position(1, 1)), Lexer("==").nextLexeme())
        assertEquals(Lexeme(Token.Assign, Position(1, 1)), Lexer("=").nextLexeme())

        assertEquals(Lexeme(Token.LessThan, Position(1, 2)), Lexer(" <").nextLexeme())
    }

    @Test
    fun lex_parenthesis() {
        assertEquals(Lexeme(Token.LeftParenthesis, Position(1, 1)), Lexer("(").nextLexeme())
        assertEquals(Lexeme(Token.RightParenthesis, Position(1, 1)), Lexer(")").nextLexeme())

        assertEquals(Lexeme(Token.LeftParenthesis, Position(1, 2)), Lexer(" (").nextLexeme())
    }

    @Test
    fun lex_identifier() {
        assertEquals(Lexeme(Token.Identifier, Position(1, 1), "abcd"), Lexer("abcd").nextLexeme())
        assertEquals(Lexeme(Token.Identifier, Position(1, 1), "abcd34"), Lexer("abcd34").nextLexeme())
        assertEquals(Lexeme(Token.Identifier, Position(1, 1), "a_bcd34"), Lexer("a_bcd34").nextLexeme())
        assertEquals(Lexeme(Token.Identifier, Position(1, 1), "_bcd34"), Lexer("_bcd34").nextLexeme())

        assertEquals(Lexeme(Token.Identifier, Position(1, 3), "_bcd34"), Lexer("  _bcd34").nextLexeme())
    }

    @Test
    fun lex_number() {
        assertEquals(Lexeme(Token.Number, Position(1, 1), "1"), Lexer("1").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "1234"), Lexer("1234").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "12.3"), Lexer("12.3").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), ".123"), Lexer(".123").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "0.123"), Lexer("0.123").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "1.123e5"), Lexer("1.123e5").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "2.123e7"), Lexer("2.123E7").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "1.123e5"), Lexer("1.123e+5").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "1.123e-5"), Lexer("1.123e-5").nextLexeme())

        assertEquals(Lexeme(Token.Number, Position(1, 1), "123"), Lexer("123a").nextLexeme())
        assertEquals(Lexeme(Token.Number, Position(1, 1), "12"), Lexer("12 3").nextLexeme())
    }

    @Test
    fun lex_nothing() {
        assertEquals(Lexeme(Token.EndOfInput, Position(1, 1)), Lexer("").nextLexeme())
        assertEquals(Lexeme(Token.EndOfInput, Position(1, 2)), Lexer(" ").nextLexeme())
        assertEquals(Lexeme(Token.NewLine, Position(1, 1)), Lexer("\n").nextLexeme())
        assertEquals(Lexeme(Token.NewLine, Position(1, 3)), Lexer("  \n  \n   ").nextLexeme())
    }

    @Test
    fun lex_simple_sum() {
        val results = Lexer("1 + 2 + 3").allLexemes()
        val expected = listOf(
                Lexeme(Token.Number, Position(1, 1), "1"),
                Lexeme(Token.Plus, Position(1, 3)),
                Lexeme(Token.Number, Position(1, 5), "2"),
                Lexeme(Token.Plus, Position(1, 7)),
                Lexeme(Token.Number, Position(1, 9), "3"),
                Lexeme(Token.EndOfInput, Position(1, 10))
        )
        assertEquals(expected, results)
    }

    @Test
    fun lex_code() {
        val results = Lexer("var a = 1 + 2 + 3\na/87*(12 -7)\nif ( a < 12 ) print a").allLexemes()
        val expected = listOf(
                Lexeme(Token.Identifier, Position(1, 1), "var"),
                Lexeme(Token.Identifier, Position(1, 5), "a"),
                Lexeme(Token.Assign, Position(1, 7)),
                Lexeme(Token.Number, Position(1, 9), "1"),
                Lexeme(Token.Plus, Position(1, 11)),
                Lexeme(Token.Number,  Position(1, 13), "2"),
                Lexeme(Token.Plus, Position(1, 15)),
                Lexeme(Token.Number, Position(1, 17), "3"),
                Lexeme(Token.NewLine, Position(1, 18)),
                Lexeme(Token.Identifier, Position(2, 1), "a"),
                Lexeme(Token.Divide, Position(2, 2)),
                Lexeme(Token.Number, Position(2, 3), "87"),
                Lexeme(Token.Times, Position(2, 5)),
                Lexeme(Token.LeftParenthesis, Position(2, 6)),
                Lexeme(Token.Number, Position(2, 7), "12"),
                Lexeme(Token.Minus, Position(2, 10)),
                Lexeme(Token.Number, Position(2, 11), "7"),
                Lexeme(Token.RightParenthesis,  Position(2, 12)),
                Lexeme(Token.NewLine, Position(2, 13)),
                Lexeme(Token.Identifier, Position(3, 1), "if"),
                Lexeme(Token.LeftParenthesis,  Position(3, 4)),
                Lexeme(Token.Identifier, Position(3, 6), "a"),
                Lexeme(Token.LessThan, Position(3, 8)),
                Lexeme(Token.Number, Position(3, 10), "12"),
                Lexeme(Token.RightParenthesis, Position(3, 13)),
                Lexeme(Token.Identifier, Position(3, 15), "print"),
                Lexeme(Token.Identifier, Position(3, 21), "a"),
                Lexeme(Token.EndOfInput, Position(3, 22))
        )
        assertEquals(expected, results)
    }
}