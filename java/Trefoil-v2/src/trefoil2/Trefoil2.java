package trefoil2;

import parser.PSTParser;

import java.io.*;

/**
 * Interpreter for the Trefoil v2 language.
 */
public class Trefoil2 {
    /**
     * Main command-line entry point.
     *
     * Takes either 0 or 1 argument on command line.
     * - If 0 args passed, reads from standard input (keyboard)
     * - If 1 arg passed, opens that arg as a file and reads from it.
     *
     * Implements the semantics of Trefoil v2 programs: reads bindings
     * one by one off the input and executes them on the dynamic environment.
     */
    public static void main(String[] args) {
        Reader inputReader = null;
        if (args.length == 0) {
            inputReader = new InputStreamReader(System.in);
        } else if (args.length == 1) {
            try {
                inputReader = new FileReader(args[0]);
            } catch (FileNotFoundException e) {
                System.err.println("Could not find file " + args[0]);
                System.exit(1);
            }
        } else {
            System.err.println("Expected 0 or 1 arguments but got " + args.length);
            System.exit(1);
        }

        PSTParser parser = PSTParser.ofReader(inputReader);

        Interpreter.DynamicEnvironment env = new Interpreter.DynamicEnvironment();
        while (true) {
            // Note that a TrefoilError will not cause this loop to exit!
            // This is good for interactive use so that users can try again.
            try {
                ParenthesizedSymbolTree pstree = parser.parse();
                if (pstree == null) {
                    break;
                }
                // System.out.println(pstree);  // uncomment to see the PST
                Binding b = Binding.parsePST(pstree);
                //System.out.println(b);  // uncomment to see the parsed binding
                env = Interpreter.interpretBinding(b, env);
            } catch (TrefoilError e) {
                System.out.println(e.getMessage());
            }
        }

        // print the environment
        System.out.println("final environment:");
        System.out.println(env);  // Uses the default Lombok toString. It's fine to leave as is, even though it is verbose.
    }

    /**
     * Throw a subclass of this error whenever your interpreter detects a problem that is the user's fault.
     */
    public abstract static class TrefoilError extends RuntimeException {
        public TrefoilError(String message) {
            super(message);
        }

        public static class LexicalError extends TrefoilError {
            public LexicalError(String message) { super(message); }
        }

        public static class ParenthesizedSyntaxError extends TrefoilError {
            public ParenthesizedSyntaxError(String message) { super(message); }
        }

        public static class AbstractSyntaxError extends TrefoilError {
            public AbstractSyntaxError(String message) { super(message); }
        }

        public static class RuntimeError extends TrefoilError {
            public RuntimeError(String message) { super(message); }
        }
    }

    /**
     * Throw this error whenever your interpreter encounters an "impossible" situation,
     * or in other words, an error that is the interpreter implementor's fault.
     *
     * This should ~never happen. But having this class available is useful for debugging,
     * and in case you feel like you need to throw an exception, but it's the user probably
     * can't help you.
     */
    public static class InternalInterpreterError extends RuntimeException {
        public InternalInterpreterError(String message) { super(message); }
        public InternalInterpreterError(Throwable cause) { super(cause); }
    }

}
