package hp.compiler.lexer

import hp.compiler.CompilationException
import hp.compiler.Lexeme
import hp.compiler.Position
import hp.compiler.Token
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
    var position: Position = Position(0, 0)

    fun nextLexeme(): Lexeme {

        if (!sentStartLexeme) {
            sentStartLexeme = true
            return Lexeme(Token.StartOfInput, position)
        }

        position = Position(row, col)

        skipWhitespaces()
        if (pos >= input.length) {
            return Lexeme(Token.EndOfInput, position)
        }

        val character = input[pos]

        if (character == '\n') {
            val token = Lexeme(Token.NewLine, position)
            pos++
            row++
            col = 1
            return token
        }

        val token = Token.values()
                .filter { it.symbol != null }
                .sortedByDescending { it.symbol!!.length }
                .firstOrNull { input.startsWith(it.symbol!!, pos) }

        val length = token?.symbol?.length ?: 0
        val nextCharacter = if (input.length > pos + length + 1) input[pos + length] else null

        var keepProcessing = false
        if (nextCharacter != null && token != null && ((
                        token == Token.Dereference && nextCharacter.isDigit()) || (
                        token.isKeyword && nextCharacter.isLetterOrDigit()))) {
            // The point started a number, not a dereference
            keepProcessing = true
        }

        if (token != null && !keepProcessing) {
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
            val value = result.joinToString("")
                    .toLowerCase()
                    .replace("+", "")

            val token = if (value.startsWith("0b")) Token.Binary
            else if (value.startsWith("0x")) Token.Hex
            else if (value.contains(".")) Token.Float
            else Token.Int

            val lexeme = Lexeme(token, Position(row, col), if(token == Token.Binary || token == Token.Hex) value.substring(2) else value)

            pos += result.size
            col += result.size

            return lexeme
        } else {
            throw IllegalStateException("Unexpected compiler error when inspecting number at row $row and col $col")
        }
    }

    enum class NumberRecogniserState : State {
        Initial,
        LeadingZero,
        Binary,
        Hex,
        Integer,
        BeginNumberWithFractionalPart,
        NumberWithFractionalPart,
        NoNextState
    }

    private fun buildNumberRecognizer() = FSM<NumberRecogniserState, Char>(
            NumberRecogniserState.Initial,
            NumberRecogniserState.NoNextState) { state, it ->
        when (state) {
            NumberRecogniserState.Initial ->
                when {
                    it == '0' -> NumberRecogniserState.LeadingZero
                    it.isDigit() -> NumberRecogniserState.Integer
                    it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                    else -> throw IllegalStateException()
                }
            NumberRecogniserState.Integer ->
                when {
                    it.isDigit() -> NumberRecogniserState.Integer
                    it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                    else -> NumberRecogniserState.NoNextState
                }
            NumberRecogniserState.BeginNumberWithFractionalPart ->
                if (it.isDigit()) NumberRecogniserState.NumberWithFractionalPart
                else throw IllegalStateException()
            NumberRecogniserState.NumberWithFractionalPart ->
                when {
                    it.isDigit() -> NumberRecogniserState.NumberWithFractionalPart
                    else -> NumberRecogniserState.NoNextState
                }
            NumberRecogniserState.LeadingZero -> when {
                it == 'b' -> NumberRecogniserState.Binary
                it == 'x' -> NumberRecogniserState.Hex
                it.isDigit() -> NumberRecogniserState.Integer
                it == '.' -> NumberRecogniserState.BeginNumberWithFractionalPart
                else -> NumberRecogniserState.NoNextState
            }
            NumberRecogniserState.Binary -> when {
                it == '0' || it == '1' -> NumberRecogniserState.Binary
                else -> NumberRecogniserState.NoNextState
            }
            NumberRecogniserState.Hex -> when {
                it in '0'..'9' || it in 'a'..'f' -> NumberRecogniserState.Hex
                else -> NumberRecogniserState.NoNextState
            }
            else -> throw IllegalStateException()
        }
    }
}