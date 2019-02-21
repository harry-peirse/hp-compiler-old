package hp.compiler

fun compileBlocks(code: String, defaultType: BlockType = BlockType.CURLY_BRACES, parent: Block? = null): Block {
    val children = mutableListOf<Block>()
    val block = Block(code, defaultType, parent, children)
    var depth = 0
    var lastBlockEnd = 0
    var currentType = defaultType

    var skip = 0

    for (i in 0 until code.length) {
        if (skip > 0) {
            skip--
            continue
        }

        println("$i  $defaultType  $depth     ${code.substring(i)}")

        typeLoop@ for (t in BlockType.values()) {
            currentType = t
            var foundType = false
            if (code.startsWith(currentType.delimiters.first, i)) {
                println("\n    Starting: $currentType")
                foundType = true
                depth++
                skip = currentType.delimiters.first.length - 1
            } else if (code.startsWith(currentType.delimiters.second, i)) {
                println("    Ending: $currentType\n")
                foundType = true
                depth--
                if (depth == 0) {
                    val childCode =
                        code.substring(
                            code.indexOf(
                                currentType.delimiters.first,
                                lastBlockEnd
                            ) + currentType.delimiters.first.length, i
                        )
                    val child = compileBlocks(childCode, currentType, block)
                    children.add(child)
                    lastBlockEnd = i + 1
                }
                skip = currentType.delimiters.second.length - 1
            }
            if (foundType) break@typeLoop
        }
    }

    return block
}

enum class BlockType(val delimiters: Pair<String, String>) {
    COMMENTED("/*" to "*/"),
    CURLY_BRACES("{" to "}"),
    BRACKETS("(" to ")"),
    SQUARE_BRACKETS("[" to "]"),
    ANGLE_BRACKETS("<" to ">")
//    DOUBLE_QUOTES("\"" to "\""),
//    QUOTES("'" to "'"),
//    TICKS("`" to "`")
}

class Block(
    var raw: String = "",
    val type: BlockType = BlockType.COMMENTED,
    val parent: Block? = null,
    var children: MutableList<Block> = mutableListOf()
) {
    val depth: Int
        get() = parent?.depth?.plus(1) ?: 0

    override fun equals(other: Any?): Boolean {
        if (other !is Block) return false
        if (other.type != type) return false
        if (raw != other.raw) return false
        return children == other.children
    }

    override fun hashCode(): Int {
        return raw.hashCode() + type.hashCode() + children.hashCode()
    }

    override fun toString(): String {
        return "\n" +
                "  ".repeat(depth) +
                "Block(type = $type, raw = \"$raw\", " +
                "children = ${if (children.isEmpty()) "()" else "(${children.joinToString("")}\n${"  ".repeat(depth)})"}"
    }
}