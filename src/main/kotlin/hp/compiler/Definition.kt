package hp.compiler

/**GENERAL DEFINITIONS **/

class CompilationException(message: String, position: Position?) : Exception("$position $message") {
    constructor(message: String, lexeme: Lexeme) : this("Lexeme(${lexeme.token} ${lexeme.value}) $message", lexeme.position)
}


/** LEXICAL DEFINITIONS **/

enum class Token(val symbol: kotlin.String? = null,
                 val isKeyword: kotlin.Boolean = false,
                 val highPriority: kotlin.Boolean = false,
                 val isBinaryOperator: kotlin.Boolean = false,
                 val isPrefixOperator: kotlin.Boolean = false,
                 val isPostfixOperator: kotlin.Boolean = false,
                 val isValueType: kotlin.Boolean = false,
                 val isAssignment: kotlin.Boolean = false,
                 val isVariable: kotlin.Boolean = false) {
    // Literals
    Byte(isValueType = true),
    Character(isValueType = true),
    Short(isValueType = true),
    Integer(isValueType = true),
    Long(isValueType = true),
    Float(isValueType = true),
    Double(isValueType = true),
    True("true", isKeyword = true, isValueType = true),
    False("false", isKeyword = true, isValueType = true),
    String(isValueType = true),

    // Identifiers
    Identifier(isValueType = true, isVariable = true),

    // Arithmetic Operators
    Plus("+", isBinaryOperator = true, isPrefixOperator = true),
    Minus("-", isBinaryOperator = true, isPrefixOperator = true),
    Times("*", highPriority = true, isBinaryOperator = true),
    Divide("/", highPriority = true, isBinaryOperator = true),
    Increment("++", isPrefixOperator = true, isPostfixOperator = true),
    Decrement("--", isPrefixOperator = true, isPostfixOperator = true),

    // Comparison Operators
    GreaterThan(">", isBinaryOperator = true),
    GreaterThanOrEqualTo(">=", isBinaryOperator = true),
    LessThan("<", isBinaryOperator = true),
    LessThanOrEqualTo("<=", isBinaryOperator = true),
    EqualTo("==", isBinaryOperator = true),
    NotEqualTo("!=", isBinaryOperator = true),

    // Logical Operators
    And("&&", isBinaryOperator = true),
    Or("||", isBinaryOperator = true),
    XOr("|", isBinaryOperator = true),
    Not("!", isPrefixOperator = true),

    // Assignment Operators
    Assign("=", isAssignment = true),
    PlusAssign("+=", isBinaryOperator = true, isAssignment = true),
    MinusAssign("-=", isBinaryOperator = true, isAssignment = true),
    TimesAssign("*=", isBinaryOperator = true, isAssignment = true),
    DivideAssign("/=", isBinaryOperator = true, isAssignment = true),

    // Parenthesis
    LeftParenthesis("("),
    RightParenthesis(")"),

    // Scope
    LeftBrace("{"),
    RightBrace("}"),

    // Special Tokens
    Dereference("."),
    NewLine("\n"),
    StartOfInput,
    EndOfInput;
}

data class Position(val row: Int = -1, val col: Int = -1, val sourceFile: String = "_") {
    override fun toString() = "Position($row, $col of $sourceFile)"
}

data class Lexeme(val token: Token, val position: Position? = null, val value: String? = null) {
    override fun toString() = "Lexeme($token at $position ${value?.trim() ?: ""})"
}

/** ABSTRACT SYNTAX TREE DEFINITIONS **/

interface AST {
    val lexeme: Lexeme
}

interface Expression : AST
interface Operator : AST
interface Unary : Operator {
    var child: AST?
}

interface Binary : Operator {
    var left: AST?
    var right: AST?
}

interface Scoped : Expression {
    var end: Lexeme?
}

data class ScopedExpression(override val lexeme: Lexeme,
                            override var end: Lexeme? = null,
                            override var child: AST? = null) : Unary, Scoped

data class BinaryOperator(override val lexeme: Lexeme,
                          override var left: AST? = null,
                          override var right: AST? = null) : Binary

data class PrefixOperator(override val lexeme: Lexeme,
                          override var child: AST? = null) : Unary

data class PostfixOperator(override val lexeme: Lexeme,
                          override var child: AST? = null) : Unary

data class Literal(override val lexeme: Lexeme) : Expression
data class Variable(override val lexeme: Lexeme) : Expression