package hp.compiler

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import hp.compiler.Lexer.Companion.isOperator
import hp.compiler.Lexer.Companion.isParenthesis
import kotlin.test.assertEquals

class TestLexer {

    @Test(expected = CompilationException::class)
    fun compilation_exception() {
        Lexer("@").nextToken()
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
        assertEquals(Token(TokenType.LessThan, "<", 1, 1), Lexer("<").nextToken())
        assertEquals(Token(TokenType.LessThanOrEqualTo, "<=", 1, 1), Lexer("<=").nextToken())
        assertEquals(Token(TokenType.GreaterThan, ">", 1, 1), Lexer(">").nextToken())
        assertEquals(Token(TokenType.GreaterThanOrEqualTo, ">=", 1, 1), Lexer(">=").nextToken())
        assertEquals(Token(TokenType.EqualTo, "==", 1, 1), Lexer("==").nextToken())
        assertEquals(Token(TokenType.Assign, "=", 1, 1), Lexer("=").nextToken())

        assertEquals(Token(TokenType.LessThan, "<", 2, 1), Lexer("\n<").nextToken())
        assertEquals(Token(TokenType.LessThan, "<", 1, 2), Lexer(" <").nextToken())
        assertEquals(Token(TokenType.LessThanOrEqualTo, "<=", 3, 4), Lexer("  \n\n   <= \n  ").nextToken())
    }

    @Test
    fun lex_parenthesis() {
        assertEquals(Token(TokenType.LeftParenthesis, "(", 1, 1), Lexer("(").nextToken())
        assertEquals(Token(TokenType.RightParenthesis, ")", 1, 1), Lexer(")").nextToken())

        assertEquals(Token(TokenType.LeftParenthesis, "(", 2, 1), Lexer("\n(").nextToken())
        assertEquals(Token(TokenType.LeftParenthesis, "(", 1, 2), Lexer(" (").nextToken())
        assertEquals(Token(TokenType.LeftParenthesis, "(", 3, 4), Lexer("  \n\n   ( \n  ").nextToken())
    }

    @Test
    fun lex_identifier() {
        assertEquals(Token(TokenType.Identifier, "abcd", 1, 1), Lexer("abcd").nextToken())
        assertEquals(Token(TokenType.Identifier, "abcd34", 1, 1), Lexer("abcd34").nextToken())
        assertEquals(Token(TokenType.Identifier, "a_bcd34", 1, 1), Lexer("a_bcd34").nextToken())
        assertEquals(Token(TokenType.Identifier, "_bcd34", 1, 1), Lexer("_bcd34").nextToken())

        assertEquals(Token(TokenType.Identifier, "_bcd34", 1, 3), Lexer("  _bcd34").nextToken())
        assertEquals(Token(TokenType.Identifier, "_bcd34", 2, 2), Lexer(" \n _bcd34").nextToken())
    }

    @Test
    fun lex_number() {
        assertEquals(Token(TokenType.Number, "1", 1, 1), Lexer("1").nextToken())
        assertEquals(Token(TokenType.Number, "1234", 1, 1), Lexer("1234").nextToken())
        assertEquals(Token(TokenType.Number, "12.3", 1, 1), Lexer("12.3").nextToken())
        assertEquals(Token(TokenType.Number, ".123", 1, 1), Lexer(".123").nextToken())
        assertEquals(Token(TokenType.Number, "0.123", 1, 1), Lexer("0.123").nextToken())
        assertEquals(Token(TokenType.Number, "1.123e5", 1, 1), Lexer("1.123e5").nextToken())
        assertEquals(Token(TokenType.Number, "2.123e7", 1, 1), Lexer("2.123E7").nextToken())
        assertEquals(Token(TokenType.Number, "1.123e5", 1, 1), Lexer("1.123e+5").nextToken())
        assertEquals(Token(TokenType.Number, "1.123e-5", 1, 1), Lexer("1.123e-5").nextToken())

        assertEquals(Token(TokenType.Number, "123", 1, 1), Lexer("123a").nextToken())
        assertEquals(Token(TokenType.Number, "12", 1, 1), Lexer("12 3").nextToken())
    }

    @Test
    fun lex_nothing() {
        assertEquals(Token(TokenType.EndOfInput, "", -1, -1), Lexer("").nextToken())
        assertEquals(Token(TokenType.EndOfInput, "", -1, -1), Lexer(" ").nextToken())
        assertEquals(Token(TokenType.EndOfInput, "", -1, -1), Lexer("\n").nextToken())
        assertEquals(Token(TokenType.EndOfInput, "", -1, -1), Lexer("  \n  \n   ").nextToken())
    }

    @Test
    fun lex_simple_sum() {
        val results = Lexer("1 + 2 + 3").allTokens()
        val expected = listOf(
                Token(TokenType.Number, "1", 1, 1),
                Token(TokenType.Plus, "+", 1, 3),
                Token(TokenType.Number, "2", 1, 5),
                Token(TokenType.Plus, "+", 1, 7),
                Token(TokenType.Number, "3", 1, 9)
        )
        assertEquals(expected, results)
    }

    @Test
    fun lex_code() {
        val results = Lexer("var a = 1 + 2 + 3\na/87*(12 -7)\nif ( a < 12 ) print a").allTokens()
        val expected = listOf(
                Token(TokenType.Identifier, "var",      1, 1),
                Token(TokenType.Identifier, "a",        1, 5),
                Token(TokenType.Assign, "=",            1, 7),
                Token(TokenType.Number, "1",            1, 9),
                Token(TokenType.Plus, "+",              1, 11),
                Token(TokenType.Number, "2",            1, 13),
                Token(TokenType.Plus, "+",              1, 15),
                Token(TokenType.Number, "3",            1, 17),
                Token(TokenType.Identifier, "a",        2, 1),
                Token(TokenType.Divide, "/",            2, 2),
                Token(TokenType.Number, "87",           2, 3),
                Token(TokenType.Times, "*",             2, 5),
                Token(TokenType.LeftParenthesis, "(",   2, 6),
                Token(TokenType.Number, "12",           2, 7),
                Token(TokenType.Minus, "-",             2, 10),
                Token(TokenType.Number, "7",            2, 11),
                Token(TokenType.RightParenthesis, ")",  2, 12),
                Token(TokenType.Identifier, "if",       3, 1),
                Token(TokenType.LeftParenthesis, "(",   3, 4),
                Token(TokenType.Identifier, "a",        3, 6),
                Token(TokenType.LessThan, "<",          3, 8),
                Token(TokenType.Number, "12",           3, 10),
                Token(TokenType.RightParenthesis, ")",  3, 13),
                Token(TokenType.Identifier, "print",    3, 15),
                Token(TokenType.Identifier, "a",        3, 21)
        )
        assertEquals(expected, results)
    }
}