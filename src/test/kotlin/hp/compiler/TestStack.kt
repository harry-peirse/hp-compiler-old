package hp.compiler

import kotlin.test.*

class TestStack {
    @Test
    fun `stack operations`() {
        val stack = Stack<String>()
        assertEquals(0, stack.size)
        assertTrue(stack.isEmpty())
        assertFalse(stack.isNotEmpty())

        stack.push("Hello World!")
        assertEquals(1, stack.size)
        assertFalse(stack.isEmpty())
        assertTrue(stack.isNotEmpty())

        assertEquals("Hello World!", stack.peek())
        assertEquals(1, stack.size)
        assertFalse(stack.isEmpty())
        assertTrue(stack.isNotEmpty())

        stack.push("Foo Bar")
        assertEquals(2, stack.size)
        assertFalse(stack.isEmpty())
        assertTrue(stack.isNotEmpty())

        assertEquals("Foo Bar", stack.peek())
        assertEquals(2, stack.size)
        assertFalse(stack.isEmpty())
        assertTrue(stack.isNotEmpty())

        assertEquals("Foo Bar", stack.pop())
        assertEquals(1, stack.size)
        assertFalse(stack.isEmpty())
        assertTrue(stack.isNotEmpty())

        assertEquals("Hello World!", stack.pop())
        assertEquals(0, stack.size)
        assertTrue(stack.isEmpty())
        assertFalse(stack.isNotEmpty())
    }

    @Test
    fun `stack equality`() {
        val stack1 = Stack<Int>()
        val stack2 = Stack<Int>()

        stack1.push(3)
        stack1.push(1)
        stack1.push(4)

        stack2.push(3)
        stack2.push(1)
        stack2.push(4)

        assertEquals(stack1, stack2)
        assertEquals(stack1.hashCode(), stack2.hashCode())

        stack2.pop()

        assertNotEquals(stack1, stack2)
        assertNotEquals(stack1.hashCode(), stack2.hashCode())
    }

    @Test(NoSuchElementException::class)
    fun `peek empty stack`() {
        val stack = Stack<Any>()
        stack.peek()
    }

    @Test(NoSuchElementException::class)
    fun `pop empty stack`() {
        val stack = Stack<Any>()
        stack.pop()
    }
}