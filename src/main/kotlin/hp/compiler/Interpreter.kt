//package hp.compiler
//
//class Interpreter {
//
//    val memory: MutableMap<String, AST> = mutableMapOf()
//
//    fun AST.evaluate(): List<String> = when (this) {
//        is Literal ->           listOf(this.evaluate().toString())
//        is Variable ->          listOf(this.evaluate().toString())
//        is UnaryOperator ->     listOf(this.evaluate().toString())
//        is BinaryOperator ->    listOf(this.evaluate().toString())
//        is ScopedExpression ->  listOf(this.evaluate().toString())
//        is Scope ->             this.evaluate()
//    }
//
//    fun Literal.evaluate(): Float? = when (token.type) {
//        TokenType.Number -> token.value?.toFloat()
//        else -> throw IllegalStateException()
//    }
//
//    fun Variable.evaluate(): Float? = when (token.type) {
//        TokenType.Identifier -> memory[token.value]?.evaluate()?.get(0)?.toFloat()
//                ?: throw CompilationException("Variable was used before it was declared", token)
//        else -> throw IllegalStateException()
//    }
//
//    fun UnaryOperator.evaluate(): Float? = when (token.type) {
//        TokenType.Minus -> {
//            val result = child?.evaluate()?.get(0)?.toFloat()
//            if (result != null) -result
//            else throw CompilationException("Null Reference", token)
//        }
//        TokenType.Plus -> {
//            val result = child?.evaluate()?.get(0)?.toFloat()
//            result ?: throw CompilationException("Null Reference", token)
//        }
//        else -> throw IllegalStateException()
//    }
//
//    fun BinaryOperator.evaluate(): Float? = when (token.type) {
//        TokenType.Minus -> {
//            val l = left?.evaluate()?.get(0)?.toFloat()
//            val r = right?.evaluate()?.get(0)?.toFloat()
//            if (l != null && r != null) l - r
//            else throw CompilationException("Null Reference", token)
//        }
//        TokenType.Plus -> {
//            val l = left?.evaluate()?.get(0)?.toFloat()
//            val r = right?.evaluate()?.get(0)?.toFloat()
//            if (l != null && r != null) l + r
//            else throw CompilationException("Null Reference", token)
//        }
//        TokenType.Times -> {
//            val l = left?.evaluate()?.get(0)?.toFloat()
//            val r = right?.evaluate()?.get(0)?.toFloat()
//            if (l != null && r != null) l * r
//            else throw CompilationException("Null Reference", token)
//        }
//        TokenType.Divide -> {
//            val l = left?.evaluate()?.get(0)?.toFloat()
//            val r = right?.evaluate()?.get(0)?.toFloat()
//            if (l != null && r != null) l / r
//            else throw CompilationException("Null Reference", token)
//        }
//        TokenType.Assign -> {
//            memory[left!!.token.value!!] = right!!
//            null
//        }
//        else -> throw IllegalStateException()
//    }
//
//    fun ScopedExpression.evaluate(): Float? = child?.evaluate()?.get(0)?.toFloat()
//
//    fun Scope.evaluate(): List<String> = statements.flatMap{ it.evaluate() }
//
//    fun run(vararg raw: String): String = raw
//            .map { AST_FSM(Lexer(it).allLexemes()).parse()?.evaluate() }
//            .filterNotNull()
//            .joinToString("\n")
//}