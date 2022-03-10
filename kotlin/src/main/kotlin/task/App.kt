package task
import java.io.File
import java.io.InputStream
import java.util.*

const val EOF_SYMBOL = -1
const val ERROR_STATE = 0
const val SKIP_VALUE = 0
const val NUMBERS = "0123456789"
const val ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz"
const val ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val WHITESPACES = " \n\t\r"

const val NEWLINE = '\n'.code

interface Automaton {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, symbol: Int): Int
    fun value(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object Example : Automaton {
    override val states = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 20, 22, 24, 26, 31) //14 je zraven zaradi whitespacov

    private val numberOfStates = states.maxOrNull()!! + 1
    private val numberOfSymbols = alphabet.maxOrNull()!! + 1
    private val transitions = Array(numberOfStates) {IntArray(numberOfSymbols)}
    private val values: Array<Int> = Array(numberOfStates) {0}

    private fun setTransition(from: Int, symbol: String, to: Int) {
        for(i in 0..symbol.length - 1) {
            transitions[from][symbol[i].code] = to
        }
    }

    private fun setValue(state: Int, terminal: Int) {
        values[state] = terminal
    }

    override fun next(state: Int, symbol: Int): Int =
        if (symbol == EOF_SYMBOL) ERROR_STATE
        else {
            assert(states.contains(state))
            assert(alphabet.contains(symbol))
            transitions[state][symbol]
        }

    override fun value(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }

    init {
        setTransition(1, WHITESPACES, 14) //to je da ignorira whitespace

        //FLOAT
        setTransition(1, NUMBERS, 2)
        setTransition(2, NUMBERS, 2)
        setTransition(2, ".", 3)
        setTransition(3, NUMBERS, 4)
        setTransition(4, NUMBERS, 4)

        setValue(2, 1)
        setValue(4, 1)
        //!FLOAT

        //VARIABLE

        setTransition(1, "abceghijklmnopqrsuvwxyz", 5) //vse brez f, t, d zaradi for, to, do, done
        setTransition(1, "ABCDEFGHIJKLMNOPQRSTUVXYZ", 5) //vse brez W zaradi WRITE
        //setTransition(1, ALPHABET_LOWER, 5)
        //setTransition(1, ALPHABET_UPPER, 5)
        setTransition(5, ALPHABET_LOWER, 5)
        setTransition(5, ALPHABET_UPPER, 5)
        setTransition(5, NUMBERS, 6)
        setTransition(6, NUMBERS, 6)

        setValue(5, 2)
        setValue(6, 2)
        //!VARIABLE

        //PLUS
        setTransition(1, "+", 7)
        setValue(7, 3)
        //!PLUS

        //MINUS
        setTransition(1, "-", 8)
        setValue(8, 4)
        //!MINUS

        //TIMES
        setTransition(1, "*", 9)
        setValue(9, 5)
        //!TIMES

        //DIVIDE
        setTransition(1, "/", 10)
        setValue(10, 6)
        //!DIVIDE

        //POW
        setTransition(1, "^", 11)
        setValue(11, 7)
        //!POW

        //LPAREN
        setTransition(1, "(", 12)
        setValue(12, 8)
        //!LPAREN

        //RPAREN
        setTransition(1, ")", 13)
        setValue(13, 9)
        //!RPAREN


        //DODATNA
            //:=
            setTransition(1, ":", 15) //15 je, ker so 14 whitespaci
            setTransition(15, "=", 16)

            setValue(16, 10)
            //!:=

            //;
            setTransition(1, ";", 17)

            setValue(17, 11)
            //!;

            //for
            setTransition(1, "f", 18);
            setTransition(18, "o", 19);
            setTransition(19, "r", 20);

            setTransition(18, "abcdefghijklmnpqrstuvwxyz", 5) //vse brez malega o-ja -> pol se obravnava kot variable
            setTransition(18, ALPHABET_UPPER, 5)
            setTransition(18, NUMBERS, 5)

            setTransition(19, "abcdefghijklmnopqstuvwxyz", 5) //vse brez malega r-ja -> pol se obravnava kot variable
            setTransition(19, ALPHABET_UPPER, 5)
            setTransition(19, NUMBERS, 5)

            setTransition(20, ALPHABET_LOWER, 5) //to je npr "fore", zdaj je "fore" ime variable
            setTransition(20, ALPHABET_UPPER, 5)
            setTransition(20, NUMBERS, 5)

            setValue(20, 12); //20 je koncno stanje za "for"
            //!for

            //do
            setTransition(1, "d", 21)
            setTransition(21, "o", 22)

            setTransition(21, "abcdefghijklmnpqrstuvwxyz", 5)
            setTransition(21, ALPHABET_UPPER, 5)
            setTransition(21, NUMBERS, 5)

            setTransition(22, "abcdefghijklmopqrstuvwxyz", 5) //brez n-ja zaradi "done"
            setTransition(22, ALPHABET_UPPER, 5)
            setTransition(22, NUMBERS, 5)

            setValue(22, 13) //22 je koncno stanje za do
            //!do

            //done
            //do do ze mamo implementirano v 22
            setTransition(22, "n", 23)
            setTransition(23, "e", 24)

            setTransition(23, "abcdfghijklmnopqrstuvwxyz", 5)
            setTransition(23, ALPHABET_UPPER, 5)
            setTransition(23, NUMBERS, 5)

            setTransition(24, ALPHABET_LOWER, 5)//to je npr za "donei", zdaj je "donei" ime variable
            setTransition(24, ALPHABET_UPPER, 5)
            setTransition(24, NUMBERS, 5)

            setValue(24, 14) //24 je koncno stanje za done
            //!done

            //to
            setTransition(1, "t", 25)
            setTransition(25, "o", 26)

            setTransition(25, "abcdefghijklmnpqrstuvwxyz", 5)
            setTransition(25, ALPHABET_UPPER, 5)
            setTransition(25, NUMBERS, 5)

            setTransition(26, ALPHABET_LOWER, 5) //to je npr za "toi", zdaj bo "toi" ime variable
            setTransition(26, ALPHABET_UPPER, 5)
            setTransition(26, NUMBERS, 5)

            setValue(26, 15) //26 je koncno stanje za to
            //!to

            //WRITE
            setTransition(1, "W", 27)
            setTransition(27, "R", 28)
            setTransition(28, "I", 29)
            setTransition(29, "T", 30)
            setTransition(30, "E", 31)

            setTransition(27, ALPHABET_LOWER, 5)
            setTransition(27, "ABCDEFGHIJKLMNOPQSTUVWXYZ", 5) //brez R-ja
            setTransition(27, NUMBERS, 5)

            setTransition(28, ALPHABET_LOWER, 5)
            setTransition(28, "ABCDEFGHJKLMNOPQRSTUVWXYZ", 5) //brez I-ja
            setTransition(28, NUMBERS, 5)

            setTransition(29, ALPHABET_LOWER, 5)
            setTransition(29, "ABCDEFGHIJKLMNOPQRSUVWXYZ", 5) //brez T-ja
            setTransition(29, NUMBERS, 5)

            setTransition(30, ALPHABET_LOWER, 5)
            setTransition(30, "ABCDFGHIJKLMNOPQRSUVWXYZ", 5) //brez E-ja
            setTransition(30, NUMBERS, 5)

            setTransition(31, ALPHABET_LOWER, 5) //to je za npr "WRITEi", zdaj je "WRITEi" ime variable
            setTransition(31, ALPHABET_UPPER, 5)
            setTransition(31, NUMBERS, 5)

            setValue(31, 16) //31 je koncno stanje za WRITE
        //!DODATNA


        /*
        setTransition(1, 'b', 2)
        setTransition(2, 'a', 1)
        setTransition(2, 'b', 2)
        setTransition(2, 'c', 3)
        setTransition(2, 'd', 4)
        setTransition(2, 'e', 5)

        setValue(3, 1);
        setValue(5, 2);

         */
    }
}

data class Token(val value: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: Automaton, private val stream: InputStream) {
    private var state = automaton.startState
    private var last: Int? = null
    private var buffer = LinkedList<Byte>()
    private var row = 1
    private var column = 1

    private fun updatePosition(symbol: Int) {
        if (symbol == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    private fun getValue(): Int {
        var symbol = last ?: stream.read()
        state = automaton.startState

        while (true) {
            updatePosition(symbol)

            val nextState = automaton.next(state, symbol)
            if (nextState == ERROR_STATE) {
                if (automaton.finalStates.contains(state)) {
                    last = symbol
                    return automaton.value(state)
                } else throw Error("Invalid pattern at ${row}:${column}")
            }
            state = nextState
            buffer.add(symbol.toByte())
            symbol = stream.read()
        }
    }

    fun eof(): Boolean =
        last == EOF_SYMBOL

    fun getToken(): Token? {
        if (eof()) return null

        val startRow = row
        val startColumn = column
        buffer.clear()

        val value = getValue()
        return if (value == SKIP_VALUE)
            getToken()
        else
            Token(value, String(buffer.toByteArray()), startRow, startColumn)
    }
}

fun name(value: Int) =
    when (value) {
        1 -> "float"
        2 -> "variable"
        3 -> "plus"
        4 -> "minus"
        5 -> "times"
        6 -> "divide"
        7 -> "pow"
        8 -> "lparen"
        9 -> "rparen"
        10 -> "assign"
        11 -> "semi"
        12 -> "for"
        13 -> "do"
        14 -> "done"
        15 -> "to"
        16 -> "write"
        else -> throw Error("Invalid value")
    }

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token != null) {
        print("${name(token.value)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}

fun readFileDirectlyAsText(fileName: String): String
        = File(fileName).readText(Charsets.UTF_8)

fun main(args: Array<String>) {
    val wholeText: String = readFileDirectlyAsText(args[0])
    val scanner = Scanner(Example, (wholeText).byteInputStream())
    printTokens(scanner)
}
