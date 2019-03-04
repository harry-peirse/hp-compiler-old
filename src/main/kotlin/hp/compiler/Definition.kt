package hp.compiler

class CompilationException(message: String, position: Position?) : Exception("$position $message") {
    constructor(message: String, lexeme: Lexeme) : this("Lexeme(${lexeme.token} ${lexeme.value}) $message", lexeme.position)
}

enum class Token(val symbol: String? = null, val highPriority: Boolean = false) {
    // Literals
    Number,

    // Identifiers
    Identifier,

    // Arithmetic Operators
    Plus("+"),
    Minus( "-"),
    Times( "*", true),
    Divide("/", true),

    // Comparison Operators
    GreaterThan(">"),
    GreaterThanOrEqualTo(">="),
    LessThan( "<"),
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

interface Expression
interface Operator
interface Scoped

data class ScopedExpression(var startLexeme: Lexeme, var endLexeme: Lexeme? = null, var child: Expression? = null): Expression, Scoped
data class BinaryOperator(var lexeme: Lexeme, var left: Expression? = null, var right: Expression? = null): Expression, Operator
data class UnaryOperator(var lexeme: Lexeme, var child: Expression? = null): Expression, Operator
data class Value(var lexeme: Lexeme): Expression