package hp.compiler

import java.lang.IllegalStateException

interface State

class FSM<T : State>(val initialState: T, val exitState: T, var acceptingStates: Array<T>, val nextState: (T, Char) -> T) {
    fun run(input: String): Pair<Boolean, String?> {
        var currentState = initialState
        var result = ""

        input.forEach {
            val nextState = nextState(currentState, it)

            if (nextState === exitState) {
                return@run true to result
            }

            result += it
            currentState = nextState
        }

        return acceptingStates.contains(currentState) to result
    }
}

class Lexer(val input: String) {

    companion object {
        private val operatorRegex = Regex("[+|=|\\-|<|>|*|/]")
        fun Char.isOperator() = operatorRegex.matches(this.toString())

        private val parenthesisRegex = Regex("[(|)]")
        fun Char.isParenthesis() = parenthesisRegex.matches(this.toString())

        private val comparisonOperatorRegex = Regex("[<|>|=]")
        fun Char.isComparisonOperator() = comparisonOperatorRegex.matches(this.toString())

        private val arithmeticOperatorRegex = Regex("[+|\\-|/|*]")
        fun Char.isArithmeticOperator() = arithmeticOperatorRegex.matches(this.toString())
    }

    var position = 0
    var line = 1
    var column = 0

    fun nextToken(): Token {

        skipWhitespacesAndNewLines()

        if (position >= input.length) {
            return Token(TokenType.EndOfInput, "", -1, -1)
        }

        val character = input[position]

        if (character.isLetter() || character == '_') {
            return recognizeIdentifier()
        }

        if (character.isDigit() || character == '.') {
            return recognizeNumber()
        }

        if (character.isOperator()) {
            return recognizeOperator()
        }

        if (character.isParenthesis()) {
            return recognizeParenthesis()
        }

        throw CompilationException("Unexpected character '$character' at line $line column $column")
    }

    fun allTokens(): List<Token> {
        var token = nextToken()
        val tokens = mutableListOf<Token>()

        while (token.type !== TokenType.EndOfInput) {
            tokens.add(token)
            token = nextToken()
        }
        return tokens
    }

    private fun skipWhitespacesAndNewLines() {
        if (position < input.length) {
            var character = input[position]
            while (position < input.length && character.isWhitespace()) {
                position++

                if (character == '\n') {
                    line++
                    column = 0
                } else {
                    column++
                }

                if (position < input.length) character = input[position]
            }
        }
    }

    private fun recognizeIdentifier(): Token {
        var identifier = ""
        var pos = position
        while (pos < input.length) {
            val character = input[pos]

            if (!(character.isLetterOrDigit() || character == '_')) {
                break
            }

            identifier += character
            pos++
        }

        val col = column + 1
        position += identifier.length
        column += identifier.length

        return Token(TokenType.Identifier, identifier, line, col)
    }

    private fun recognizeNumber(): Token {
        // We delegate the building of the FSM to a helper method.
        val fsm = buildNumberRecognizer()

        // The input to the FSM will be all the characters from
        // the current position to the rest of the lexer's input.
        val fsmInput = input.substring(position)

        // Here, in addition of the FSM returning whether a number
        // has been recognized or not, it also returns the number
        // recognized in the 'number' variable. If no number has
        // been recognized, 'number' will be 'null'.
        val result = fsm.run(fsmInput)
        val isNumberRecognized = result.first
        val number = result.second

        if (isNumberRecognized && number != null) {
            val col = column + 1
            position += number.length
            column += number.length

            return Token(TokenType.Number, number.toLowerCase().replace("+", ""), line, col)
        } else {
            throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
        }
    }

    enum class NumberRecogniserState : State {
        Initial,
        Integer,
        BeginNumberWithFractionalPart,
        NumberWithFractionalPart,
        BeginNumberWithExponent,
        BeginNumberWithSignedExponent,
        NumberWithExponent,
        NoNextState
    }

    private fun buildNumberRecognizer() = FSM(
            NumberRecogniserState.Initial,
            NumberRecogniserState.NoNextState,
            arrayOf(NumberRecogniserState.Integer,
                    NumberRecogniserState.NumberWithFractionalPart,
                    NumberRecogniserState.NumberWithExponent
            )) { currentState, it ->
        when (currentState) {
            NumberRecogniserState.Initial ->
                when {
                    it.isDigit() -> NumberRecogniserState.Integer
                    it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                    else -> throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
                }
            NumberRecogniserState.Integer ->
                when {
                    it.isDigit() -> NumberRecogniserState.Integer
                    it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                    it.toLowerCase() == 'e' -> NumberRecogniserState.BeginNumberWithExponent
                    else -> NumberRecogniserState.NoNextState
                }
            NumberRecogniserState.BeginNumberWithFractionalPart ->
                if (it.isDigit()) NumberRecogniserState.NumberWithFractionalPart
                else throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
            NumberRecogniserState.NumberWithFractionalPart ->
                when {
                    it.isDigit() -> NumberRecogniserState.NumberWithFractionalPart
                    it.toLowerCase() == 'e' -> NumberRecogniserState.BeginNumberWithExponent
                    else -> NumberRecogniserState.NoNextState
                }
            NumberRecogniserState.BeginNumberWithExponent ->
                when {
                    it == '+' || it == '-' -> NumberRecogniserState.BeginNumberWithSignedExponent
                    it.isDigit() -> NumberRecogniserState.NumberWithExponent
                    else -> throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
                }
            NumberRecogniserState.BeginNumberWithSignedExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
            NumberRecogniserState.NumberWithExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else NumberRecogniserState.NoNextState
            NumberRecogniserState.NoNextState -> throw IllegalStateException("Unexpected compiler error when inspecting number at line $line and column $column")
        }
    }

    private fun recognizeOperator(): Token {
        val character = input[position]
        return when {
            character.isComparisonOperator() -> recognizeComparisonOperator()
            character.isArithmeticOperator() -> recognizeArithmeticOperator()
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at line $line and column $column")
        }
    }

    private fun recognizeComparisonOperator(): Token {
        val character = input[position]

        // 'lookahead' is the next character in the input
        // or 'null' if 'character' was the last character.
        val lookahead = if (position + 1 < input.length) input[position + 1] else null

        // Whether the 'lookahead' character is the equal symbol '='.
        val isLookaheadEqualSymbol = lookahead !== null && lookahead == '='

        position++
        column++

        val col = column
        if (isLookaheadEqualSymbol) {
            position++
            column++
        }

        return when (character) {
            '>' -> if (isLookaheadEqualSymbol) Token(TokenType.GreaterThanOrEqualTo, ">=", line, col) else Token(TokenType.GreaterThan, ">", line, col)
            '<' -> if (isLookaheadEqualSymbol) Token(TokenType.LessThanOrEqualTo, "<=", line, col) else Token(TokenType.LessThan, "<", line, col)
            '=' -> if (isLookaheadEqualSymbol) Token(TokenType.EqualTo, "==", line, col) else Token(TokenType.Assign, "=", line, col)
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at line $line and column $col")
        }
    }

    private fun recognizeArithmeticOperator(): Token {
        val character = input[position]

        position++
        column++

        return when (character) {
            '+' -> Token(TokenType.Plus, "+", line, column)
            '-' -> Token(TokenType.Minus, "-", line, column)
            '*' -> Token(TokenType.Times, "*", line, column)
            '/' -> Token(TokenType.Divide, "/", line, column)
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at line $line and column $column")
        }
    }

    fun recognizeParenthesis(): Token {
        val character = input[position]

        position++
        column++

        return when (character) {
            '(' -> Token(TokenType.LeftParenthesis, "(", line, column)
            ')' -> Token(TokenType.RightParenthesis, ")", line, column)
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at line $line and column $column")
        }
    }
}