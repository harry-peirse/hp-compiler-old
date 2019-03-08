package hp.compiler

/**GENERAL DEFINITIONS **/

class CompilationException(message: String) : Exception(message) {
    constructor(message: String, position: Position) : this("$message $position")
    constructor(message: String, lexeme: Lexeme) : this("$message: '${lexeme.token.symbol
            ?: lexeme.value}' ${lexeme.position}")
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
                 val isVariable: kotlin.Boolean = false,
                 val isLiteral: kotlin.Boolean = false) {
    // Literals
    Byte(isValueType = true, isLiteral = true),
    Character(isValueType = true, isLiteral = true),
    Short(isValueType = true, isLiteral = true),
    Integer(isValueType = true, isLiteral = true),
    Long(isValueType = true, isLiteral = true),
    Float(isValueType = true, isLiteral = true),
    Double(isValueType = true, isLiteral = true),
    True("true", isKeyword = true, isValueType = true, isLiteral = true),
    False("false", isKeyword = true, isValueType = true, isLiteral = true),
    String(isValueType = true, isLiteral = true),

    // Identifiers
    Identifier(isValueType = true, isVariable = true),

    // Arithmetic Operators
    Plus("+", isBinaryOperator = true, isPrefixOperator = true),
    Minus("-", isBinaryOperator = true, isPrefixOperator = true),
    Times("*", highPriority = true, isBinaryOperator = true),
    Divide("/", highPriority = true, isBinaryOperator = true),
    Power("^", highPriority = true, isBinaryOperator = true),
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
    TypeDenotation(":", isAssignment = true),
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

    // Keywords
    Var("var", isKeyword = true),

    // Special Tokens
    Dereference("."),
    Comma(","),
    Semicolon(";"),
    NewLine("\n"),
    StartOfInput,
    EndOfInput;
}

data class Position(val row: Int = -1, val col: Int = -1, val sourceFile: String = "") {
    override fun toString() = "$sourceFile@$row,$col"
}

data class Lexeme(val token: Token, val position: Position? = null, val value: String? = null) {
    override fun toString() = "$position $token ${if (value == null) "" else "'$value'"}"
}

/** ABSTRACT SYNTAX TREE DEFINITIONS **/

interface Parser<T : AST> {
    val finished: Boolean
    val ast: T
    fun input(lexeme: Lexeme)
}

interface Lexed {
    val lexeme: Lexeme
}

sealed class AST : Lexed {
    var parent: AST? = null
    open val type: Type = Types.Nothing
    val depth: Int
        get() {
            var count = 0
            var p = this
            while (p.parent != null) {
                p = p.parent!!
                count++
            }
            return count
        }
    val depthString: String
        get() = "    ".repeat(depth)

    open val children: List<AST> = emptyList()
    fun linkHierarchy() {
        children.forEach {
            it.parent = this
            it.linkHierarchy()
        }
    }
}

object Types {
    val Byte = Type("Byte", 2)
    val Char = Type("Char", 3)
    val Short = Type("Short", 4)
    val Int = Type("Int", 5)
    val Long = Type("Long", 6)
    val Float = Type("Float", 7)
    val Double = Type("Double", 8)
    val Boolean = Type("Boolean")
    val String = Type("String")
    val Nothing = Type("Nothing", Integer.MAX_VALUE)
}

data class Scope(override val lexeme: Lexeme,
                 var end: Lexeme? = null,
                 override val children: MutableList<AST> = mutableListOf()) : AST() {
    override fun toString(): String = "\n${depthString}Scope $lexeme to $end ${children.joinToString("")}"
}

data class ScopedExpression(override val lexeme: Lexeme,
                            var end: Lexeme? = null,
                            var expression: AST? = null) : AST() {
    override val children get() = listOfNotNull(expression)
    override val type get() = expression?.type ?: Types.Nothing
    override fun toString(): String = "\n${depthString}ScopedExpression $lexeme to $end $expression"
}

data class BinaryOperator(override val lexeme: Lexeme,
                          var left: AST? = null,
                          var right: AST? = null) : AST() {
    override val children get() = listOfNotNull(left, right)
    override val type
        get() = if (left?.type ?: Types.Nothing > right?.type ?: Types.Nothing) left?.type
                ?: Types.Nothing else right?.type ?: Types.Nothing

    override fun toString(): String = "\n${depthString}BinaryOperator   $lexeme $left $right"
}

data class PrefixOperator(override val lexeme: Lexeme,
                          var child: AST? = null) : AST() {
    override val children get() = listOfNotNull(child)
    override val type get() = child?.type ?: Types.Nothing
    override fun toString(): String = "\n${depthString}PrefixOperator   $lexeme $child"
}

data class Declaration(override val lexeme: Lexeme,
                       var variable: AST? = null,
                       var expression: AST? = null) : AST() {
    override val children get() = listOfNotNull(variable, expression)
    override fun toString(): String = "\n${depthString}Declaration      $lexeme $variable $expression"
}

data class Assignment(override val lexeme: Lexeme,
                      var variable: AST? = null,
                      var expression: AST? = null) : AST() {
    override val children get() = listOfNotNull(variable, expression)
    override fun toString(): String = "\n${depthString}Assignment       $lexeme $variable $expression"
}

data class PostfixOperator(override val lexeme: Lexeme,
                           var child: AST? = null) : AST() {
    override val children get() = listOfNotNull(child)
    override val type get() = child?.type ?: Types.Nothing
    override fun toString(): String = "\n${depthString}PostfixOperator  $lexeme $child"
}

data class Literal(override val lexeme: Lexeme) : AST() {
    override val type
        get() = when (lexeme.token) {
            Token.Byte -> Types.Byte
            Token.Character -> Types.Char
            Token.Short -> Types.Short
            Token.Integer -> Types.Int
            Token.Long -> Types.Long
            Token.Float -> Types.Float
            Token.Double -> Types.Double
            Token.True, Token.False -> Types.Boolean
            Token.String -> Types.String
            else -> throw CompilationException("Unexpected type", lexeme)
        }

    override fun toString(): String = "\n${depthString}Literal         (lexeme=$lexeme)"
}

data class Variable(override val lexeme: Lexeme) : AST() {
    override fun toString(): String = "\n${depthString}Variable        (lexeme=$lexeme)"
}

data class Type(val name: String, val rank: Int = 0) : Comparable<Type> {
    override fun compareTo(other: Type) = other.rank.compareTo(rank)
}