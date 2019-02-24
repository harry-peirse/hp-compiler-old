package hp.compiler

import kotlin.test.Test
import kotlin.test.assertEquals

class TestAbstractSyntaxTree {

    @Test
    fun simple_sum() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "1"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+"))
        expected.left = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
        expected.right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))

        assertEquals(expected, result)
    }

    @Test
    fun unary_expresion() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "56")
        )).parse()

        val expected = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-")).apply {
            child = LeafNode(ASTState.Number, Token(TokenType.Number, "56"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun unary_operator_with_sum() {
        // "-2 + 3"
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "2"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "3")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-")).apply {
                child = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
            }
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "3"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun complex_sum() {
        // "-56 + -3 + 2"
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Minus, "-", 1, 1),
                Token(TokenType.Number, "56", 2, 1),
                Token(TokenType.Plus, "+", 5, 1),
                Token(TokenType.Minus, "-", 7, 1),
                Token(TokenType.Number, "3", 8, 1),
                Token(TokenType.Plus, "+", 10, 1),
                Token(TokenType.Number, "2", 12, 1)
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+", 5, 1)).apply {
            left = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-", 1, 1)).apply {
                child = LeafNode(ASTState.Number, Token(TokenType.Number, "56", 2, 1))
            }
            right = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+", 10, 1)).apply {
                left = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-", 7, 1)).apply {
                    child = LeafNode(ASTState.Number, Token(TokenType.Number, "3", 8, 1))
                }
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "2", 12, 1))
            }
        }

        assertEquals(expected, result)
    }

    @Test
    fun more_complex_sum() {
        // "-56 * -3 + 2"
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "56"),
                Token(TokenType.Times, "*"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "3"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = BinaryNode(ASTState.Operator, Token(TokenType.Times, "*")).apply {
                left = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-")).apply {
                    child = LeafNode(ASTState.Number, Token(TokenType.Number, "56"))
                }
                right = UnaryNode(ASTState.UnaryOperator, Token(TokenType.Minus, "-")).apply {
                    child = LeafNode(ASTState.Number, Token(TokenType.Number, "3"))
                }
            }
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun _12_plus_4_times_2() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "12"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "4"),
                Token(TokenType.Times, "*"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = LeafNode(ASTState.Number, Token(TokenType.Number, "12"))
            right = BinaryNode(ASTState.Operator, Token(TokenType.Times, "*")).apply {
                left = LeafNode(ASTState.Number, Token(TokenType.Number, "4"))
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
            }
        }

        assertEquals(expected, result)
    }

    @Test
    fun _4_times_2_plus_12() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "4"),
                Token(TokenType.Times, "*"),
                Token(TokenType.Number, "2"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "12")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = BinaryNode(ASTState.Operator, Token(TokenType.Times, "*")).apply {
                left = LeafNode(ASTState.Number, Token(TokenType.Number, "4"))
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
            }
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "12"))
        }

        assertEquals(expected, result)
    }
}
