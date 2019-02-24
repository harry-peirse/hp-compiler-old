package hp.compiler

class CompilationException(message: String, row: Int, col: Int) : Exception("At row $row and col $col $message") {
    constructor(message: String, token: Token): this("token ${token.type} ${token.value} $message", token.row ?: -1, token.col ?: -1)
}

enum class TokenType(val orderOfOperationsPriority: Int = -1) {
    // Literals
    Number,

    // Identifiers
    Identifier,

    // Arithmetic Operators
    Plus(100),
    Minus(100),
    Times(10),
    Divide(10),

    // Comparison Operators
    GreaterThan,
    GreaterThanOrEqualTo,
    LessThan,
    LessThanOrEqualTo,
    EqualTo,

    // Assignment Operators
    Assign,

    // Parenthesis
    LeftParenthesis(1),
    RightParenthesis(1),

    // Special Tokens
    EndOfInput
}

data class Token(val type: TokenType, val value: String, val row: Int? = null, val col: Int? = null)