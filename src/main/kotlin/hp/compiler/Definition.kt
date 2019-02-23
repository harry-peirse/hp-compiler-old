package hp.compiler

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

data class Token(val type: TokenType, val value: String, val row: Int, val col: Int)