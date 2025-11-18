package parser;

import lombok.Getter;
import trefoil2.Trefoil2;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Wraps a Reader so that the most recently read character is buffered in the next field.
 *
 * Also tracks line number and column number information.
 *
 * Clients can call getNext() to look at the "next" character. advance() causes the next
 * character to be consumed and another character to be read from the underlying source.
 */
public class PeekCharReader {
    /**
     * The next character available on the input, or -1 if EOF.
     */
    @Getter
    private int next = 0;
    @Getter
    private int lineNumber = 1;
    @Getter
    private int columnNumber = -1;
    private final Reader reader;

    public PeekCharReader(Reader reader) {
        this.reader = reader;
        advance();
    }

    public int advance() {
        try {
            // System.out.println("  reading...");
            next = reader.read();
            // System.out.println("  ...read '" + (char)next + "'");
            columnNumber++;
            if (next == '\n') {
                lineNumber++;
                columnNumber = -1;
            }
            return next;
        } catch (IOException e) {
            throw new Trefoil2.InternalInterpreterError(e);
        }
    }

    // Convenience factory method used by unit tests.
    public static PeekCharReader fromString(String s) {
        return new PeekCharReader(new StringReader(s));
    }
}
