package hp.compiler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BlockTests {

    @Test
    fun testBlockEquals() {
        assertNotEquals(Block("hi"), Block("hello"))
        assertNotEquals(Block("hi", BlockType.ANGLE_BRACKETS), Block("hi", BlockType.BRACKETS))
        assertEquals(Block(), Block())
        assertEquals(Block("foo", BlockType.SQUARE_BRACKETS), Block("foo", BlockType.SQUARE_BRACKETS))
        assertNotEquals(Block("hey"), Block("hey", children = mutableListOf(Block())))
        assertEquals(
            Block("hey", children = mutableListOf(Block("foo"), Block("bar"))),
            Block("hey", children = mutableListOf(Block("foo"), Block("bar")))
        )
        assertNotEquals(
            Block("hey", children = mutableListOf(Block("foo"), Block("bar"))),
            Block("hey", children = mutableListOf(Block("bar"), Block("foo")))
        )
    }

    @Test
    fun testBlockHashCode() {
        assertNotEquals(Block("hi").hashCode(), Block("hello").hashCode())
        assertEquals(Block().hashCode(), Block().hashCode())
        assertEquals(Block("foo").hashCode(), Block("foo").hashCode())
        assertNotEquals(Block("hey").hashCode(), Block("hey", children = mutableListOf(Block())).hashCode())
        assertEquals(
            Block("hey", children = mutableListOf(Block("foo"), Block("bar"))).hashCode(),
            Block("hey", children = mutableListOf(Block("foo"), Block("bar"))).hashCode()
        )
        assertNotEquals(
            Block("hey", children = mutableListOf(Block("foo"), Block("bar"))).hashCode(),
            Block("hey", children = mutableListOf(Block("bar"), Block("foo"))).hashCode()
        )
    }

    class BlockBuilder {
        var raw = ""
        var type = BlockType.COMMENTED
        private val children = mutableListOf<BlockBuilder>()

        fun child(builder: BlockBuilder.() -> Unit) = children.add(BlockBuilder().apply(builder))

        operator fun invoke(parent: Block? = null): Block {
            val b = Block(raw, type, parent)
            b.children = children.map { it(b) }.toMutableList()
            return b
        }
    }

    fun block(builder: BlockBuilder.() -> Unit) = BlockBuilder().apply(builder)()

    @Test
    fun testOnlyCurlyBraces() {
        val testCode = " 0 { 0.1 { 0.1.1 { 0.1.1.1 /0.1.1.1 } /0.1.1 } { 0.1.2 /0.1.2 } /0.1 } /0 "
        val expected = block {
            raw = " 0 { 0.1 { 0.1.1 { 0.1.1.1 /0.1.1.1 } /0.1.1 } { 0.1.2 /0.1.2 } /0.1 } /0 "
            type = BlockType.CURLY_BRACES
            child {
                raw = " 0.1 { 0.1.1 { 0.1.1.1 /0.1.1.1 } /0.1.1 } { 0.1.2 /0.1.2 } /0.1 "
                type = BlockType.CURLY_BRACES
                child {
                    raw = " 0.1.1 { 0.1.1.1 /0.1.1.1 } /0.1.1 "
                    type = BlockType.CURLY_BRACES
                    child {
                        raw = " 0.1.1.1 /0.1.1.1 "
                        type = BlockType.CURLY_BRACES
                    }
                }
                child {
                    raw = " 0.1.2 /0.1.2 "
                    type = BlockType.CURLY_BRACES
                }
            }
        }
        val result = compileBlocks(testCode)
        assertEquals(expected, result)
    }

    @Test
    fun testComment() {
        val testCode = "/* comment */ { /* another [1,2,3] comment */ ('a') }"
        val expected = block {
            raw = "/* comment */ { /* another [1,2,3] comment */ ('a') }"
            type = BlockType.CURLY_BRACES
            child {
                raw = " comment "
                type = BlockType.COMMENTED
            }
            child {
                raw = " /* another [1,2,3] comment */ ('a') "
                type = BlockType.CURLY_BRACES
                child {
                    raw = " another [1,2,3] comment "
                    type = BlockType.COMMENTED
                    child {
                        raw = "1,2,3"
                        type = BlockType.SQUARE_BRACKETS
                    }
                }
                child {
                    raw = "'a'"
                    type = BlockType.BRACKETS
                }
            }
        }
        val result = compileBlocks(testCode)
        assertEquals(expected, result)
    }

    @Test
    fun testNoComments() {
        val testCode = " ( another [1,2,3] expression ) ('a') "
        val expected = block {
            raw = " ( another [1,2,3] expression ) ('a') "
            type = BlockType.CURLY_BRACES
            child {
                raw = " another [1,2,3] expression "
                type = BlockType.BRACKETS
                child {
                    raw = "1,2,3"
                    type = BlockType.SQUARE_BRACKETS
                }
            }
            child {
                raw = "'a'"
                type = BlockType.BRACKETS
            }
        }
        val result = compileBlocks(testCode)
        assertEquals(expected, result)
    }

    @Test
    fun testSymbolMadness() {
        val testCode = "{<>[]{(){/*{}/**/*/}}}"
        val expected = block {
            raw = "{<>[]{(){/*{}/**/*/}}}"
            type = BlockType.CURLY_BRACES
            child {
                raw = "<>[]{(){/*{}/**/*/}}"
                type = BlockType.CURLY_BRACES
                child { type = BlockType.ANGLE_BRACKETS }
                child { type = BlockType.SQUARE_BRACKETS }
                child {
                    raw = "(){/*{}/**/*/}"
                    type = BlockType.CURLY_BRACES
                    child { type = BlockType.BRACKETS }
                    child {
                        raw = "/*{}/**/*/"
                        type = BlockType.CURLY_BRACES
                        child {
                            raw = "{}/**/"
                            type = BlockType.COMMENTED
                            child { type = BlockType.CURLY_BRACES }
                            child { type = BlockType.COMMENTED }
                        }
                    }
                }
            }
        }
        val result = compileBlocks(testCode)
        assertEquals(expected, result)
    }
}