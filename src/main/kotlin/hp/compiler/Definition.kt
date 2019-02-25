package hp.compiler

class CompilationException(message: String, row: Int, col: Int) : Exception("At row $row and col $col $message") {
    constructor(message: String, token: Token): this("token ${token.type} ${token.value} $message", token.row ?: -1, token.col ?: -1)
}

enum class TokenType(val orderOfOperationsPriority: Int) {
    // Literals
    Number(0),
    Boolean(0),
    Character(0),

    // Identifiers
    Identifier(0),

    // Arithmetic Operators
    Plus(100),
    Minus(100),
    Times(10),
    Divide(10),

    // Comparison Operators
    GreaterThan(1000),
    GreaterThanOrEqualTo(1000),
    LessThan(1000),
    LessThanOrEqualTo(1000),
    EqualTo(1000),

    // Assignment Operators
    Assign(10000),

    // Parenthesis
    LeftParenthesis(1),
    RightParenthesis(1),

    // Special Tokens
    EndOfInput(-1)
}

data class Token(val type: TokenType, val value: String, val row: Int? = null, val col: Int? = null)