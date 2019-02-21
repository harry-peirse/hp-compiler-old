package hp.compiler

class Lexer(val input: String) {

    var position = 0
    var line = 0
    var column = 0

    fun nextToken(): Token {
        if(position >= input.length) {
            return Token(TokenType.EndOfInput)
        }

        val character = input[position]

        return when(character) {
            isArithmeticOperator(it) -> parseArithmeticOperator(character)
        }
    }

    fun isArithmeticOperator(character: Char) = character == '+' || character == '-' || character == '*' || character == '/'

    fun parseArithmeticOperator(character: Char): Token {
        this.position += 1
        this.column += 1

        return when (character) {
            '+' -> Token(TokenType.Plus, "+", line, column)
            else -> null
        }!!
    }
}