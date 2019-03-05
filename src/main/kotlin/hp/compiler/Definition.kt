package hp.compiler

/**GENERAL DEFINITIONS **/

class CompilationException(message: String, position: Position?) : Exception("$position $message") {
    constructor(message: String, lexeme: Lexeme) : this("Lexeme(${lexeme.token} ${lexeme.value}) $message", lexeme.position)
}


/** LEXICAL DEFINITIONS **/

enum class Token(val symbol: String? = null, val highPriority: kotlin.Boolean = false) {
    // Literals
    Number,
    Boolean,

    // Identifiers
    Identifier,

    // Arithmetic Operators
    Plus("+"),
    Minus("-"),
    Times("*", true),
    Divide("/", true),

    // Comparison Operators
    GreaterThan(">"),
    GreaterThanOrEqualTo(">="),
    LessThan("<"),
    LessThanOrEqualTo("<="),
    EqualTo("=="),

    // Assignment Operators
    Assign("="),

    // Parenthesis
    LeftParenthesis("("),
    RightParenthesis(")"),

    LeftBrace("{"),
    RightBrace("}"),

    // Special Tokens
    NewLine("\n"),
    StartOfInput,
    EndOfInput;
}

data class Position(val row: Int = -1, val col: Int = -1, val sourceFile: String = "_") {
    override fun toString() = "Position($row, $col of $sourceFile)"
}

data class Lexeme(val token: Token, val position: Position? = null, val value: String? = null) {
    override fun toString() = "Lexeme($token at $position ${value?.trim() ?: ""})"
}

/** ABSTRACT SYNTAX TREE DEFINITIONS **/

interface AST {
    val lexeme: Lexeme
}

interface Expression : AST
interface Operator : AST
interface Unary<T : AST> : AST {
    var child: T?
}

interface Binary<T : AST, U : AST> : AST {
    var left: T?
    var right: T?
}

interface Scoped : AST {
    var end: Lexeme?
}

interface Arithmetic : Expression
interface ArithmeticOperator : Arithmetic, Operator

data class ScopedArithmeticExpression(override val lexeme: Lexeme, override var end: Lexeme? = null, override var child: Arithmetic? = null) :
        Unary<Arithmetic>, Arithmetic, Scoped

data class BinaryArithmeticOperator(override val lexeme: Lexeme, override var left: Arithmetic? = null, override var right: Arithmetic? = null) :
        Binary<Arithmetic, Arithmetic>, ArithmeticOperator

data class UnaryArithmeticOperator(override val lexeme: Lexeme, override var child: Arithmetic? = null) :
        Unary<Arithmetic>, ArithmeticOperator

data class ArithmeticValue(override val lexeme: Lexeme) :
        Arithmetic