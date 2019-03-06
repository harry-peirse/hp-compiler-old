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
    NewLine("\n"),
    StartOfInput,
    EndOfInput;
}

data class Position(val row: Int = -1, val col: Int = -1, val sourceFile: String = "") {
    override fun toString() = "$sourceFile@${row.toString().padStart(3, '0')},${col.toString().padStart(3, '0')}"
}

data class Lexeme(val token: Token, val position: Position? = null, val value: String? = null) {
    override fun toString() = "$position ${token.toString().padEnd(16)} ${(value ?: "").padStart(5)}"
}

/** ABSTRACT SYNTAX TREE DEFINITIONS **/

interface Parser<T: AST> {
    val ast: T
    fun input(lexeme: Lexeme)
}

sealed class AST {
    var parent: AST? = null
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

    abstract fun linkHierarchy()
}

data class ScopedExpression(val lexeme: Lexeme,
                            var end: Lexeme? = null,
                            var expression: AST? = null) : AST() {
    override fun linkHierarchy() {
        expression?.parent = this
        expression?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}ScopedExpression(lexeme=$lexeme, end=$end, expression=$expression)"
}

data class BinaryOperator(val lexeme: Lexeme,
                          var left: AST? = null,
                          var right: AST? = null) : AST() {
    override fun linkHierarchy() {
        left?.parent = this
        left?.linkHierarchy()
        right?.parent = this
        right?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}BinaryOperator  (lexeme=$lexeme, left=$left, right=$right)"
}

data class PrefixOperator(val lexeme: Lexeme,
                          var child: AST? = null) : AST() {
    override fun linkHierarchy() {
        child?.parent = this
        child?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}PrefixOperator  (lexeme=$lexeme, child=$child)"
}

data class Declaration(val lexeme: Lexeme,
                       var variable: AST? = null,
                       var expression: AST? = null) : AST() {
    override fun linkHierarchy() {
        variable?.parent = this
        variable?.linkHierarchy()
        expression?.parent = this
        expression?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}Declaration     (lexeme=$lexeme, variable=$variable, expression=$expression)"
}

data class Assignment(val lexeme: Lexeme,
                      var variable: AST? = null,
                      var expression: AST? = null) : AST() {
    override fun linkHierarchy() {
        variable?.parent = this
        variable?.linkHierarchy()
        expression?.parent = this
        expression?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}Assignment      (lexeme=$lexeme, variable=$variable, expression=$expression)"
}

data class PostfixOperator(val lexeme: Lexeme,
                           var child: AST? = null) : AST() {
    override fun linkHierarchy() {
        child?.parent = this
        child?.linkHierarchy()
    }

    override fun toString(): String = "\n${depthString}PostfixOperator (lexeme=$lexeme, child=$child)"
}

data class Literal(val lexeme: Lexeme) : AST() {
    override fun linkHierarchy() = Unit
    override fun toString(): String = "\n${depthString}Literal         (lexeme=$lexeme)"
}

data class Variable(val lexeme: Lexeme) : AST() {
    override fun linkHierarchy() = Unit
    override fun toString(): String = "\n${depthString}Variable        (lexeme=$lexeme)"
}