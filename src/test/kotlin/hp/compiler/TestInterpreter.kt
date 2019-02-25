package hp.compiler

import kotlin.test.Test
import kotlin.test.assertEquals

class TestInterpreter {
    @Test
    fun do_a_sum() {
        assertEquals("20", Interpreter().run("2*4+12"))

        assertEquals("20", Interpreter().run("2 * 4 + 12"))

        assertEquals("20", Interpreter().run("\n2 \n* 4 + 12"))

        assertEquals("20", Interpreter().run("12+2*4"))

        assertEquals("20", Interpreter().run("12+16/2"))

        assertEquals("20", Interpreter().run("-1 * -12+16/2"))

        assertEquals("2.4", Interpreter().run("2*1.2"))

        assertEquals("2.4", Interpreter().run("4.8/2"))

        assertEquals("2.1", Interpreter().run("4.2/2"))

        assertEquals("9", Interpreter().run("10 - 3 + 2"))
    }

    @Test
    fun test_memory() {
        assertEquals("", Interpreter().run("a = 4"))
    }

    @Test
    fun test_multiple_expressions() {
        val interpreter = Interpreter()
        assertEquals("6", interpreter.run("a = 4", "2 + a"))
        assertEquals<Map<String, Node>>(mapOf("a" to LeafNode(ASTState.Number, Token(TokenType.Number, "4", 1, 5))), interpreter.memory)
    }
}