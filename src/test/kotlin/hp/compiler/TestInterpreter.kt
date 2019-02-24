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
    }
}
