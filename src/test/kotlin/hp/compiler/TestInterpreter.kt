package hp.compiler

import kotlin.test.Test
import kotlin.test.assertEquals

class TestInterpreter {
    @Test
    fun do_a_sum() {
        assertEquals("20", Interpreter("2*4+12").run())

        assertEquals("20", Interpreter("2 * 4 + 12").run())

        assertEquals("20", Interpreter("\n2 \n* 4 + 12").run())

        assertEquals("20", Interpreter("12+2*4").run())

        assertEquals("20", Interpreter("12+16/2").run())

        assertEquals("20", Interpreter("-1 * -12+16/2").run())

        assertEquals("2.4", Interpreter("2*1.2").run())

        assertEquals("2.4", Interpreter("4.8/2").run())

        assertEquals("2.1", Interpreter("4.2/2").run())

        assertEquals("9", Interpreter("10 - 3 + 2").run())
    }

    @Test
    fun test_memory() {
        assertEquals("", Interpreter("a = 4").run())
    }

    @Test
    fun test_multiple_expressions() {
        val interpreter = Interpreter(
                "a = 4",
                "2 + a"
        )
        assertEquals("6", interpreter.run())

        assertEquals<Map<String, Node>>(mapOf("a" to LeafNode(ASTState.Number, Token(TokenType.Number, "4", 1, 5))), interpreter.memory)
    }
}