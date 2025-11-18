package parser;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Takes a PeekCharReader and splits its characters into parenthesized-symbol tokens.
 *
 * Clients can call getNext() to look at the next token. advance() causes the next token to
 * be consumed and the input is scanned until another token is constructed.
 *
 * Unlike PeekCharReader, the Tokenizer is initially in a not-yet-any-input state. The client
 * must explicitly call advance() when they are ready to start scanning the underlying input stream.
 * (This design makes it easier to write clients that operate on interactive input streams.)
 */
public class Tokenizer {
    public static Tokenizer tokenizeString(String s) {
        return new Tokenizer(PeekCharReader.fromString(s));
    }

    /**
     * Represents one token of a parenthesized-symbol input.
     *
     * A token is one of the following
     * - a SymbolToken (any word)
     * - a PunctuationToken (open or close parenthesis)
     * - EOFToken (end of input)
     *
     */
    @ToString
    public static abstract class Token {
        private final int lineNumber;
        private final int columnNumber;

        private Token(int lineNumber, int columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        @EqualsAndHashCode(callSuper = false)
        @ToString(callSuper = true)
        public static class SymbolToken extends Token {
            @Getter
            private final String symbol;

            public SymbolToken(String symbol) {
                this(symbol, 1, 0);
            }

            public SymbolToken(String symbol, int lineNumber, int columnNumber) {
                super(lineNumber, columnNumber);
                this.symbol = symbol;
            }
        }

        @EqualsAndHashCode(callSuper = false)
        @ToString(callSuper = true)
        public static class PunctuationToken extends Token {
            @Getter
            private final String punctuation;

            public PunctuationToken(String punctuation) {
                this(punctuation, 1, 0);
            }

            public PunctuationToken(String punctuation, int lineNumber, int columnNumber) {
                super(lineNumber, columnNumber);
                this.punctuation = punctuation;
            }
        }

        @EqualsAndHashCode(callSuper = false)
        @ToString(callSuper = true)
        public static class EOFToken extends Token {
            public EOFToken() {
                this(1, 0);
            }

            public EOFToken(int lineNumber, int columnNumber) {
                super(lineNumber, columnNumber);
            }
        }
    }

    @Getter
    private Token next;  // initially null, indicating not-yet-consumed-any-input

    private final PeekCharReader reader;

    public Tokenizer(PeekCharReader reader) {
        this.reader = reader;
        next = null;
    }

    /**
     * Consume the next token and scan underlying input until another token can be constructed.
     */
    public Token advance() {
        int c;
        while (true) {
            c = reader.getNext();
            if (c == -1) {
                next = new Token.EOFToken(reader.getLineNumber(), reader.getColumnNumber());
                break;
            } else if (Character.isWhitespace(c)) {  // skip whitespace
                reader.advance();
                // go around the loop
            } else if (c == '(') {
                next = new Token.PunctuationToken("(", reader.getLineNumber(), reader.getColumnNumber());
                reader.advance();
                break;
            } else if (c == ')') {
                next = new Token.PunctuationToken(")", reader.getLineNumber(), reader.getColumnNumber());
                reader.advance();
                break;
            } else if (c == ';') {  // detect and skip comments
                // skip to next line
                do {
                    c = reader.advance();
                } while (c != -1 && c != '\n');
            } else {
                // otherwise read the characters of a symbol
                // a symbol is terminated by EOF, whitespace, parenthesis, or semicolon
                StringBuilder symbol = new StringBuilder();
                int lineNumber = reader.getLineNumber();
                int columnNumber = reader.getColumnNumber();
                do {
                    symbol.append((char) c);
                    c = reader.advance();
                // WARNING: the following line should be kept in sync with the if-else chain above
                } while (c != -1 && !Character.isWhitespace(c) && c != '(' && c != ')' && c != ';');

                next = new Token.SymbolToken(symbol.toString(), lineNumber, columnNumber);
                break;
            }
        }
        // System.out.println("advanced to token " + next);
        return next;
    }
}
