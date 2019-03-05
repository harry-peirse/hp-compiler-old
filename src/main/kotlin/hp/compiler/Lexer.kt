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

    var pos = 0
    var row = 1
    var col = 1
    var sentStartLexeme = false

    fun nextLexeme(): Lexeme {

        if (!sentStartLexeme) {
            sentStartLexeme = true
            return Lexeme(Token.StartOfInput, Position(0, 0))
        }

        skipWhitespaces()
        if (pos >= input.length) {
            return Lexeme(Token.EndOfInput, Position(row, col))
        }

        val character = input[pos]

        if (character == '\n') {
            val token = Lexeme(Token.NewLine, Position(row, col))
            pos++
            row++
            col = 1
            return token
        }

        val position = Position(row, col)

        val token = Token.values()
                .filter { it.symbol != null }
                .sortedByDescending { it.symbol!!.length }
                .firstOrNull { input.startsWith(it.symbol!!, pos) }

        val length = token?.symbol?.length ?: 0

        if (token != null && ((!token.isKeyword || input.length < pos + length) ||
                        (token.isKeyword && input.length >= pos + length && !input[pos + length].isLetterOrDigit()))) {
            pos += length
            col += length
            return Lexeme(token, position)
        } else {

            if (character.isLetter() || character == '_') {
                return recognizeIdentifier()
            }

            if (character.isDigit() || character == '.') {
                return recognizeNumber()
            }
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
        if (pos < input.length) {
            var character = input[pos]
            while (pos < input.length && character.isWhitespace() && character != '\n') {
                pos++
                col++

                if (pos < input.length) character = input[pos]
            }
        }
    }

    private fun recognizeIdentifier(): Lexeme {
        var identifier = ""
        var pos = pos
        while (pos < input.length) {
            val character = input[pos]

            if (!(character.isLetterOrDigit() || character == '_')) {
                break
            }

            identifier += character
            pos++
        }

        val token = Lexeme(Token.Identifier, Position(row, col), identifier)
        this.pos += identifier.length
        this.col += identifier.length

        return token
    }

    private fun recognizeNumber(): Lexeme {
        // We delegate the building of the FSM to a helper method.
        val fsm = buildNumberRecognizer()

        // The input to the FSM will be all the characters from
        // the current pos to the rest of the lexer's input.
        val fsmInput = input.substring(pos)

        // Here, in addition of the FSM returning whether a number
        // has been recognized or not, it also returns the number
        // recognized in the 'number' variable. If no number has
        // been recognized, 'number' will be 'null'.
        val result = fsm.run(fsmInput.toList())

        if (result.isNotEmpty()) {
            val token = Lexeme(Token.Float, Position(row, col),
                    result.joinToString("")
                            .toLowerCase()
                            .replace("+", ""))
            pos += result.size
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
                    else -> throw IllegalStateException()
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
                else throw IllegalStateException()
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
                    else -> throw IllegalStateException()
                }
            NumberRecogniserState.BeginNumberWithSignedExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else throw IllegalStateException()
            NumberRecogniserState.NumberWithExponent ->
                if (it.isDigit()) NumberRecogniserState.NumberWithExponent
                else NumberRecogniserState.NoNextState
            else -> throw IllegalStateException()
        }
    }
}