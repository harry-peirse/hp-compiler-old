package hp.compiler

import java.util.Scanner

fun main() {

    val interpreter = Interpreter()

    System.out.println()
    System.out.println()
    System.out.println("*****************************")
    System.out.println("*-- Welcome to the HP CLI --*")
    System.out.println("*****************************")
    System.out.println()
    System.out.println("Enter 'break' to quit...")
    System.out.println()

    Scanner(System.`in`).use {

        while (true) {
            System.out.print("  > ")
            val input = it.nextLine()
            if (input == "break") break
            System.out.println("  > " + interpreter.run(input))
        }

    }
}