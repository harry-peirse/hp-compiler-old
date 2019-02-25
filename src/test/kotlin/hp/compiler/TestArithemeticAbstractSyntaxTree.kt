package hp.compiler

import kotlin.test.Test
import kotlin.test.assertEquals

class TestArithemeticAbstractSyntaxTree {

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
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "56"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "3"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
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

    @Test
    fun _10_minus_3_plus_2() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "10"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "3"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = BinaryNode(ASTState.Operator, Token(TokenType.Minus, "-")).apply {
                left = LeafNode(ASTState.Number, Token(TokenType.Number, "10"))
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "3"))
            }
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun parenthesis() {
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "10"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.LeftParenthesis, "("),
                Token(TokenType.Number, "3"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2"),
                Token(TokenType.RightParenthesis, ")")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Minus, "-")).apply {
            left = LeafNode(ASTState.Number, Token(TokenType.Number, "10"))
            right = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
                left = LeafNode(ASTState.Number, Token(TokenType.Number, "3"))
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
            }
        }

        assertEquals(expected, result)
    }

    @Test
    fun parenthesis_at_beginning() {
        // (1 + 1) * 2
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.LeftParenthesis, "("),
                Token(TokenType.Number, "1"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "1"),
                Token(TokenType.RightParenthesis, ")"),
                Token(TokenType.Times, "*"),
                Token(TokenType.Number, "2")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Times, "*")).apply {
            left = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
                left = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
                right = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
            }
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun complex_parenthesis() {
        // 10 - (3 + 2) / (2-1)
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "10"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.LeftParenthesis, "("),
                Token(TokenType.Number, "3"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Number, "2"),
                Token(TokenType.RightParenthesis, ")"),
                Token(TokenType.Divide, "/"),
                Token(TokenType.LeftParenthesis, "("),
                Token(TokenType.Number, "2"),
                Token(TokenType.Minus, "-"),
                Token(TokenType.Number, "1"),
                Token(TokenType.RightParenthesis, ")")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Minus, "-")).apply {
            left = LeafNode(ASTState.Number, Token(TokenType.Number, "10"))
            right = BinaryNode(ASTState.Operator, Token(TokenType.Divide, "/")).apply {
                left = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
                    left = LeafNode(ASTState.Number, Token(TokenType.Number, "3"))
                    right = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
                }
                right = BinaryNode(ASTState.Operator, Token(TokenType.Minus, "-")).apply {
                    left = LeafNode(ASTState.Number, Token(TokenType.Number, "2"))
                    right = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
                }
            }
        }

        assertEquals(expected, result)
    }

    @Test
    fun variable_assignment() {
        // a = 1
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Identifier, "a"),
                Token(TokenType.Assign, "="),
                Token(TokenType.Number, "1")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Assign, "=")).apply {
            left = LeafNode(ASTState.Identifier, Token(TokenType.Identifier, "a"))
            right = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
        }

        assertEquals(expected, result)
    }

    @Test
    fun variable_use() {
        // 1 + a
        val result = AbstractSyntaxTree(listOf(
                Token(TokenType.Number, "1"),
                Token(TokenType.Plus, "+"),
                Token(TokenType.Identifier, "a")
        )).parse()

        val expected = BinaryNode(ASTState.Operator, Token(TokenType.Plus, "+")).apply {
            left = LeafNode(ASTState.Number, Token(TokenType.Number, "1"))
            right = LeafNode(ASTState.Identifier, Token(TokenType.Identifier, "a"))
        }

        assertEquals(expected, result)
    }
}
