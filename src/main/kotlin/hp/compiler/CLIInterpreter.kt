//package hp.compiler
//
//import java.util.Scanner
//
//fun main() {
//
//    val interpreter = Interpreter()
//
//    System.out.println()
//    System.out.println()
//    System.out.println("*****************************")
//    System.out.println("*-- Welcome to the HP CLI --*")
//    System.out.println("*****************************")
//    System.out.println()
//    System.out.println("Enter 'break' to quit...")
//    System.out.println()
//
//    Scanner(System.`in`).use {
//        var input = it.nextLine()
//        while (input != "break") {
//            try {
//                System.out.println(interpreter.run(input))
//            } catch (e: CompilationException) {
//                System.err.println(e.message)
//            }
//            input = it.nextLine()
//        }
//    }
//}