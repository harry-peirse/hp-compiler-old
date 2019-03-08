package hp.interpreter

import java.util.*

fun main() {

    val interpreter = Interpreter()

    Scanner(System.`in`).use {
        var line: String
        do {
            line = it.nextLine()
            try {
                interpreter.interpret(line)
            } catch (e: Exception) {
                System.err.println(e.message ?: e)
                interpreter.parser = null
            }
        } while (line != "quit")
    }
}