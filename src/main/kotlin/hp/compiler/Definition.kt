package hp.compiler

enum class TokenType {
    // Literals
    Number,

    // Arithmetic Operators
    Plus,

    // Special Tokens
    EndOfInput
}

data class Token(val type: TokenType, val value: String? = null, val row: Int? = null, val col: Int? = null)
