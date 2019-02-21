package hp.compiler

import org.junit.Test
import kotlin.test.assertEquals

class TestLexer {

    @Test
    fun lex_a_simple_sum() {
        val expected = listOf(
            Token(1,1,"1"),
            Token(1,2, Operator.PLUS)
        )
        assertEquals(expected, Lexer.lex("1+2"))
    }
}