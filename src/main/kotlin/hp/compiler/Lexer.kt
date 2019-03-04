package hp.compiler

import java.lang.IllegalStateException

interface State

class FSM<S : State, T>(val initialState: S, val exitState: S, val accumulator: MutableList<T> = mutableListOf(), val nextState: (S, T) -> S) {
    fun run(input: Iterable<T>): List<T> {
        input.fold(initialState) { state, it ->
            val nextState = nextState(state, it)
            when (nextState) {
                exitState -> return@run accumulator
                else -> {
                    accumulator.add(it)
                    return@fold nextState
                }
            }
        }
        return accumulator
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
    var row = 1
    var col = 1
    var sentStartLexeme = false

    fun nextLexeme(): Lexeme {

        if(!sentStartLexeme) {
            sentStartLexeme = true
            return Lexeme(Token.StartOfInput, Position(0, 0))
        }

        skipWhitespaces()
        if (position >= input.length) {
            return Lexeme(Token.EndOfInput, Position(row, col))
        }

        val character = input[position]

        if (character == '\n') {
            val token = Lexeme(Token.NewLine, Position(row, col))
            position++
            row++
            col = 1
            return token
        }

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

        throw CompilationException("Unexpected character '$character'", Position(row, col))
    }

    fun allLexemes(): List<Lexeme> {
        var lexeme: Lexeme
        val tokens = mutableListOf<Lexeme>()

        do {
            lexeme = nextLexeme()
            tokens.add(lexeme)
        } while (lexeme.token !== Token.EndOfInput)

        return tokens
    }

    private fun skipWhitespaces() {
        if (position < input.length) {
            var character = input[position]
            while (position < input.length && character.isWhitespace() && character != '\n') {
                position++
                col++

                if (position < input.length) character = input[position]
            }
        }
    }

    private fun recognizeIdentifier(): Lexeme {
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

        val token = Lexeme(Token.Identifier, Position(row, col), identifier)
        position += identifier.length
        this.col += identifier.length

        return token
    }

    private fun recognizeNumber(): Lexeme {
        // We delegate the building of the FSM to a helper method.
        val fsm = buildNumberRecognizer()

        // The input to the FSM will be all the characters from
        // the current position to the rest of the lexer's input.
        val fsmInput = input.substring(position)

        // Here, in addition of the FSM returning whether a number
        // has been recognized or not, it also returns the number
        // recognized in the 'number' variable. If no number has
        // been recognized, 'number' will be 'null'.
        val result = fsm.run(fsmInput.toList())

        if (result.isNotEmpty()) {
            val token = Lexeme(Token.Number, Position(row, col),
                    result.joinToString("")
                            .toLowerCase()
                            .replace("+", ""))
            position += result.size
            col += result.size

            return token
        } else {
            throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
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

    private fun buildNumberRecognizer() = FSM<NumberRecogniserState, Char>(
            NumberRecogniserState.Initial,
            NumberRecogniserState.NoNextState) { state, it ->
        when (state) {
            NumberRecogniserState.Initial ->
                when {
                    it.isDigit() -> NumberRecogniserState.Integer
                    it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                    else -> throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
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
                else throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
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
                    else -> throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
                }
            NumberRecogniserState.BeginNumberWithSignedExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
            NumberRecogniserState.NumberWithExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else NumberRecogniserState.NoNextState
            NumberRecogniserState.NoNextState -> throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
        }
    }

    private fun recognizeOperator(): Lexeme {
        val character = input[position]
        return when {
            character.isComparisonOperator() -> recognizeComparisonOperator()
            character.isArithmeticOperator() -> recognizeArithmeticOperator()
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at row $row and col $col")
        }
    }

    private fun recognizeComparisonOperator(): Lexeme {
        val character = input[position]

        // 'lookahead' is the next character in the input
        // or 'null' if 'character' was the last character.
        val lookahead = if (position + 1 < input.length) input[position + 1] else null

        // Whether the 'lookahead' character is the equal symbol '='.
        val isLookaheadEqualSymbol = lookahead !== null && lookahead == '='

        val token = when (character) {
            '>' ->
                if (isLookaheadEqualSymbol) Lexeme(Token.GreaterThanOrEqualTo, Position(row, col))
                else Lexeme(Token.GreaterThan, Position(row, col))
            '<' ->
                if (isLookaheadEqualSymbol) Lexeme(Token.LessThanOrEqualTo, Position(row, col))
                else Lexeme(Token.LessThan, Position(row, col))
            '=' ->
                if (isLookaheadEqualSymbol) Lexeme(Token.EqualTo, Position(row, col))
                else Lexeme(Token.Assign,  Position(row, col))
            else ->
                throw IllegalStateException("Unexpected compiler error when inspecting '$character' at row $row and col $col")
        }

        position++
        col++

        if (isLookaheadEqualSymbol) {
            position++
            col++
        }

        return token
    }

    private fun recognizeArithmeticOperator(): Lexeme {
        val character = input[position]

        val token = when (character) {
            '+' -> Lexeme(Token.Plus, Position(row, col))
            '-' -> Lexeme(Token.Minus,  Position(row, col))
            '*' -> Lexeme(Token.Times, Position(row, col))
            '/' -> Lexeme(Token.Divide, Position(row, col))
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at row $row and col $col")
        }

        position++
        col++

        return token
    }

    fun recognizeParenthesis(): Lexeme {
        val character = input[position]

        val token = when (character) {
            '(' -> Lexeme(Token.LeftParenthesis, Position(row, col))
            ')' -> Lexeme(Token.RightParenthesis, Position(row, col))
            else -> throw IllegalStateException("Unexpected compiler error when inspecting '$character' at row $row and col $col")
        }

        position++
        col++

        return token
    }
}