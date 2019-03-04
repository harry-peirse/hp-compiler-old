//package hp.compiler
//
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class TestAST {
//
//    @Test
//    fun simple_sum() {
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = Literal(Lexeme(Token.Number, value = "1"))
//                right = Literal(Lexeme(Token.Number, value = "2"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun unary_expresion() {
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "56"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            this += (UnaryOperator(Lexeme(Token.Minus)).apply {
//                child = Literal(Lexeme(Token.Number, value = "56"))
//            })
//            endToken = Lexeme(Token.RightBrace)
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun unary_operator_with_sum() {
//        // "-2 + 3"
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = UnaryOperator(Lexeme(Token.Minus)).apply {
//                    child = Literal(Lexeme(Token.Number, value = "2"))
//                }
//                right = Literal(Lexeme(Token.Number, value = "3"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun complex_sum() {
//        // "-56 + -3 + 2"
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "56"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = BinaryOperator(Lexeme(Token.Plus)).apply {
//                    left = UnaryOperator(Lexeme(Token.Minus)).apply {
//                        child = Literal(Lexeme(Token.Number, value = "56"))
//                    }
//                    right = UnaryOperator(Lexeme(Token.Minus)).apply {
//                        child = Literal(Lexeme(Token.Number, value = "3"))
//                    }
//                }
//                right = Literal(Lexeme(Token.Number, value = "2"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun more_complex_sum() {
//        // "-56 * -3 + 2"
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "56"),
//                Lexeme(Token.Times),
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = BinaryOperator(Lexeme(Token.Times)).apply {
//                    left = UnaryOperator(Lexeme(Token.Minus)).apply {
//                        child = Literal(Lexeme(Token.Number, value = "56"))
//                    }
//                    right = UnaryOperator(Lexeme(Token.Minus)).apply {
//                        child = Literal(Lexeme(Token.Number, value = "3"))
//                    }
//                }
//                right = Literal(Lexeme(Token.Number, value = "2"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun _12_plus_4_times_2() {
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "12"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "4"),
//                Lexeme(Token.Times),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = Literal(Lexeme(Token.Number, value = "12"))
//                right = BinaryOperator(Lexeme(Token.Times)).apply {
//                    left = Literal(Lexeme(Token.Number, value = "4"))
//                    right = Literal(Lexeme(Token.Number, value = "2"))
//                }
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun _4_times_2_plus_12() {
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "4"),
//                Lexeme(Token.Times),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "12"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = BinaryOperator(Lexeme(Token.Times)).apply {
//                    left = Literal(Lexeme(Token.Number, value = "4"))
//                    right = Literal(Lexeme(Token.Number, value = "2"))
//                }
//                right = Literal(Lexeme(Token.Number, value = "12"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun _10_minus_3_plus_2() {
//        // 10-3+2
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "10"),
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = BinaryOperator(Lexeme(Token.Minus)).apply {
//                    left = Literal(Lexeme(Token.Number, value = "10"))
//                    right = Literal(Lexeme(Token.Number, value = "3"))
//                }
//                right = Literal(Lexeme(Token.Number, value = "2"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun parenthesis() {
//        // 10-(3+2)
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "10"),
//                Lexeme(Token.Minus),
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Minus)).apply {
//                left = Literal(Lexeme(Token.Number, value = "10"))
//                right = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                    endLexeme = Lexeme(Token.RightParenthesis)
//                    child = BinaryOperator(Lexeme(Token.Plus)).apply {
//                        left = Literal(Lexeme(Token.Number, value = "3"))
//                        right = Literal(Lexeme(Token.Number, value = "2"))
//                    }
//                }
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun parenthesis_at_beginning() {
//        // (1+1)*2
//        val result = AST_FSM(listOf(
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.Times),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Times)).apply {
//                left = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                    endLexeme = Lexeme(Token.RightParenthesis)
//                    child = BinaryOperator(Lexeme(Token.Plus)).apply {
//                        left = Literal(Lexeme(Token.Number, value = "1"))
//                        right = Literal(Lexeme(Token.Number, value = "1"))
//                    }
//                }
//                right = Literal(Lexeme(Token.Number, value = "2"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun complex_parenthesis() {
//        // 10 - (3 + 2) / (2-1)
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "10"),
//                Lexeme(Token.Minus),
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "3"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.Divide),
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "2"),
//                Lexeme(Token.Minus),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Minus)).apply {
//                left = Literal(Lexeme(Token.Number, value = "10"))
//                right = BinaryOperator(Lexeme(Token.Divide)).apply {
//                    left = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                        endLexeme = Lexeme(Token.RightParenthesis)
//                        child = BinaryOperator(Lexeme(Token.Plus)).apply {
//                            left = Literal(Lexeme(Token.Number, value = "3"))
//                            right = Literal(Lexeme(Token.Number, value = "2"))
//                        }
//                    }
//                    right = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                        endLexeme = Lexeme(Token.RightParenthesis)
//                        child = BinaryOperator(Lexeme(Token.Minus)).apply {
//                            left = Literal(Lexeme(Token.Number, value = "2"))
//                            right = Literal(Lexeme(Token.Number, value = "1"))
//                        }
//                    }
//                }
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun variable_assignment() {
//        // a = 1
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Identifier, value = "a"),
//                Lexeme(Token.Assign),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Assign)).apply {
//                left = Variable(Lexeme(Token.Identifier, value = "a"))
//                right = Literal(Lexeme(Token.Number, value = "1"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun variable_use() {
//        // 1 + a
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.Plus),
//                Lexeme(Token.Identifier, value = "a"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Plus)).apply {
//                left = Literal(Lexeme(Token.Number, value = "1"))
//                right = Variable(Lexeme(Token.Identifier, value = "a"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun `multiple statements`() {
//        // a = 1
//        // (43) * (98)
//        // 1
//        // -a
//        val result = AST_FSM(listOf(
//                Lexeme(Token.Identifier, value = "a"),
//                Lexeme(Token.Assign),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.NewLine),
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "43"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.Times),
//                Lexeme(Token.LeftParenthesis),
//                Lexeme(Token.Number, value = "98"),
//                Lexeme(Token.RightParenthesis),
//                Lexeme(Token.NewLine),
//                Lexeme(Token.Number, value = "1"),
//                Lexeme(Token.NewLine),
//                Lexeme(Token.Minus),
//                Lexeme(Token.Identifier, value = "a"),
//                Lexeme(Token.EndOfInput)
//        )).parse()
//
//        val expected = Scope(Lexeme(Token.LeftBrace)).apply {
//            endToken = Lexeme(Token.RightBrace)
//            this += BinaryOperator(Lexeme(Token.Assign)).apply {
//                left = Variable(Lexeme(Token.Identifier, value = "a"))
//                right = Literal(Lexeme(Token.Number, value = "1"))
//            }
//            this += BinaryOperator(Lexeme(Token.Times)).apply {
//                left = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                    endLexeme = Lexeme(Token.RightParenthesis)
//                    child = Literal(Lexeme(Token.Number, value = "43"))
//                }
//                right = ScopedExpression(Lexeme(Token.LeftParenthesis)).apply {
//                    endLexeme = Lexeme(Token.RightParenthesis)
//                    child = Literal(Lexeme(Token.Number, value = "98"))
//                }
//            }
//            this += Literal(Lexeme(Token.Number, value = "1"))
//            this += UnaryOperator(Lexeme(Token.Minus)).apply {
//                child = Variable(Lexeme(Token.Identifier, value = "a"))
//            }
//        }
//
//        assertEquals(expected, result)
//    }
//}
