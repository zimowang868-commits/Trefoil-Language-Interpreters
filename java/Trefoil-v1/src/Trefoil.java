import java.io.*;
import java.util.*;
import java.util.Scanner;

/**
 * Interpreter for the Trefoil v1 language.
 *
 * The interpreter's state is a stack of integers.
 *
 * The interpreter also implements a main method to accept input from the keyboard or a file.a
 */
public class Trefoil {
    // TODO: probably declare one or more fields to track the state of the interpreter.
    // TODO: either initialize your fields directly or create an explicit zero-argument constructor to do so.
    private Stack<Integer> stack;

    public Trefoil() {
        stack = new Stack<>();
    }

    public static void main(String[] args) {
        Trefoil trefoil = new Trefoil();

        try {
            if (args.length == 0) {
                trefoil.interpret(new Scanner(System.in));
            } else if (args.length == 1) {
                try {
                    trefoil.interpret(new Scanner(new File(args[0])));
                } catch (FileNotFoundException e) {
                    System.out.println("Error: File " + args[0] + " can't be found here");   // TODO: wrong; print nice message instead
                    System.exit(1); // TODO: wrong; exit indicating error instead
                }
            } else {
                System.err.println("Expected 0 or 1 arguments but got " + args.length);
                System.exit(1);
            }

            // print the stack
            System.out.println(trefoil);
        } catch (TrefoilError e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Interpret the program given by the scanner.
     */
    public void interpret(Scanner scanner) {
        // TODO: your interpreter here. feel free to adapt the demo code from lecture 1
        while (scanner.hasNext()) {
            if (scanner.hasNextInt()) {
                stack.push(scanner.nextInt());
            } else {
                String token = scanner.next();
                if (!token.equals(";")) {
                    if (token.equals(".")) {
                        stack.pop();
                    } else if (token.equals("+") && stack.size() >= 2) {
                        stack.push(stack.pop() + stack.pop());
                    } else if (token.equals("-") && stack.size() >= 2) {
                        int num1 = stack.pop();
                        int num2 = stack.pop();
                        stack.push(num2 - num1);
                    } else if (token.equals("*") && stack.size() >= 2) {
                        stack.push(stack.pop() * stack.pop());
                    } else {
                        throw new TrefoilError("Error: invalid character is input");
                    }
                }
            }
        }
    }

    /**
     * Convenience method to interpret the given string. Useful for unit tests.
     */
    public void interpret(String input) {
        // Don't change this method unless you know what you're doing.
        interpret(new Scanner(input));
    }

    /**
     * Pop a value off the stack and return it. Useful for unit tests.
     *
     * @throws TrefoilError if there are no elements on the stack.
     */
    public int pop() {
        // TODO: implement this
        if (stack.size() == 0) {
            throw new TrefoilError("Error: The stack is empty");
        }
        return stack.pop();
    }

    @Override
    public String toString() {
        // TODO: change this to print the stack
        //       (don't print it using Stack.toString like we did it in Lecture 1;
        //       read the spec and check the tests)
        Stack<Integer> stack1 = new Stack<>();
        while (stack.size() != 0) {
            stack1.push(stack.pop());
        }

        String string = stack1.pop() + "";
        while (stack1.size() != 0) {
            string += " " + stack1.pop();
        }
        return string;
    }

    /**
     * Throw this error whenever your interpreter detects a problem.
     *
     * TODO: Catch this error in main and print a nice message and exit in a way that indicates an error.
     */
    public static class TrefoilError extends RuntimeException {
        public TrefoilError(String message) {
            super(message);
        }
    }
}