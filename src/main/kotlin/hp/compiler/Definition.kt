package hp.compiler

class CompilationException(message: String, row: Int, col: Int) : Exception("At row $row and col $col $message") {
    constructor(message: String, token: Token): this("token ${token.type} ${token.value} $message", token.row ?: -1, token.col ?: -1)
}

enum class TokenType {
    // Literals
    Number,

    // Identifiers
    Identifier,

    // Arithmetic Operators
    Plus,
    Minus,
    Times,
    Divide,

    // Comparison Operators
    GreaterThan,
    GreaterThanOrEqualTo,
    LessThan,
    LessThanOrEqualTo,
    EqualTo,

    // Assignment Operators
    Assign,

    // Parenthesis
    LeftParenthesis,
    RightParenthesis,

    // Special Tokens
    EndOfInput
}

data class Token(val type: TokenType, val value: String, val row: Int? = null, val col: Int? = null)