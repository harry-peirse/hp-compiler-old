package hp.compiler.ast

const val DEBUG_LOGGING = true

class FSM<S, T : Any>(initialState: S, private val exitState: S, private val transitionTable: (S, T) -> S) {

    var state: S = initialState
        private set
    var finished = false
        private set

    fun input(value: T) {
        state = transitionTable(state, value)
        if (state == exitState) {
            finished = true
        }
    }
}