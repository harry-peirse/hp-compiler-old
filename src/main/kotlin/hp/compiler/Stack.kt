package hp.compiler

class Stack<T> {

    private val data = mutableListOf<T>()

    val size: Int
        get() {
            synchronized(data) {
                return data.size
            }
        }

    fun push(value: T) {
        synchronized(data) {
            data.add(value)
        }
    }

    fun pop(): T {
        synchronized(data) {
            val last = data.last()
            data.remove(last)
            return last
        }
    }

    fun peek(): T {
        synchronized(data) {
            return data.last()
        }
    }

    fun isEmpty(): Boolean {
        synchronized(data) {
            return size == 0
        }
    }

    fun isNotEmpty() = !isEmpty()

    override fun equals(other: Any?) = other is Stack<*> && other.data == data
    override fun hashCode() = data.hashCode()
    override fun toString() = data.toString()
}