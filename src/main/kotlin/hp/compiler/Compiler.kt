package hp.compiler

import java.io.File

class CompilationException(message: String) : Exception(message)

class Output {
    val stringBuilder = StringBuilder()
    val indent = "    "
    var indentLevel = 0

    operator fun unaryPlus() {
        indentLevel++
    }

    operator fun unaryMinus() {
        indentLevel--
    }

    operator fun plusAssign(code: String) {
        stringBuilder.append(indent.repeat(indentLevel) + code + "\n")
    }

    override fun toString(): String {
        return stringBuilder.toString()
    }
}

fun main(args: Array<String>) {
    val compiledFile = File(System.getProperty("user.dir") + "/hp.cpp")
    if (compiledFile.exists()) {
        if (compiledFile.isFile) {
            compiledFile.delete()
        } else if (compiledFile.isDirectory) {
            throw IllegalStateException("$compiledFile is a directory, cannot compile to here")
        }
    } else {
        compiledFile.createNewFile()
    }
    val output = Output()
    output += "#include <iostream>"
    output += ""
    output += "void main(int argc, char *argv[])"
    output += "{"
    +output

    val code = args.map { File(if (it.startsWith("/")) it else System.getProperty("user.dir") + "/$it") }
        .filter { it.exists() }
        .map { it.readText(charset("UTF8")) }
        .joinToString("\n")

    println(code)

    code.split("\n")
        .flatMap { it.split(";") }
        .map { it.trim() }
        .forEach {
            if (it.startsWith("<<!")) output += "std::cerr << \"" + (evaluateString(it.removePrefix("<<!").trim())) + "\";"
            else if (it.startsWith("<<")) output += "std::cout << \"" + (evaluateString(it.removePrefix("<<").trim())) + "\";"
        }

    -output
    output += "}"

    compiledFile.writeText(output.toString(), charset("UTF8"))
    println()
    println()
    println(compiledFile)
    println()
    println()
    println(output)
    println()
    println()
}

fun evaluateString(code: String): String {
    return code.replace("\"", "")
}