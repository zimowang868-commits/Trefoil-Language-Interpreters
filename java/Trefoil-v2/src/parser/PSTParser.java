package parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Stack;

import trefoil2.ParenthesizedSymbolTree;
import trefoil2.Trefoil2;

/**
 * Constructs a ParenthesizedSymbolTree from a tokenized input stream.
 *
 * After constructing a PSTParser, clients can call parse() repeatedly to get the PSTs.
 */
public class PSTParser {
    private final Tokenizer tokenizer;

    public PSTParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    // Convenience factory methods.
    public static PSTParser ofReader(Reader reader) {
        return new PSTParser(new Tokenizer(new PeekCharReader(reader)));
    }
    public static PSTParser parseString(String s) {
        return new PSTParser(Tokenizer.tokenizeString(s));
    }

    /**
     * Pulls tokens off the input stream until exactly one PST has been parsed, then returns it.
     */
    public ParenthesizedSymbolTree parse() {
        // The algorithm is a hand-rolled bottom-up stack-based parser.
        // The stack represents all the PSTs for which we have seen an open paren but not yet a close paren.
        Tokenizer.Token token;

        // Note that we use Node.children mutably here, while the entire rest of the codebase this field as immutable.
        // It's ok-ish because the mutations are scoped inside parse() and do not escape.
        // It might be better to use a stack of Lists and not reuse the Node class here.
        Stack<ParenthesizedSymbolTree.Node> stack = new Stack<>();

        // loop invariant: not yet advanced() to next token
        while (true) {
            token = tokenizer.advance();
            if (token instanceof Tokenizer.Token.PunctuationToken) {
                Tokenizer.Token.PunctuationToken punctuationToken = (Tokenizer.Token.PunctuationToken) token;
                if (punctuationToken.getPunctuation().equals("(")) {
                    stack.push(new ParenthesizedSymbolTree.Node(new ArrayList<>()));
                } else if (punctuationToken.getPunctuation().equals(")")) {
                    // We want to pop the stack. There are three cases:
                    // (1) The stack has nothing on it. We saw a close paren before the first open paren.
                    // (2) The stack has exactly one thing on it. Pop and return it.
                    // (3) The stack as more than one thing on it. Pop the top thing and add it to the end of the
                    //     list of children of the second thing on the stack.
                    if (stack.size() < 1) {
                        throw new Trefoil2.TrefoilError.ParenthesizedSyntaxError("Too many close parentheses: " + token);
                    }
                    ParenthesizedSymbolTree.Node node = stack.pop();
                    if (stack.size() == 0) {
                        return node;
                    } else {
                        stack.peek().getChildren().add(node);
                    }
                } else {
                    throw new Trefoil2.TrefoilError.ParenthesizedSyntaxError("Unrecognized token " + token);
                }
            } else if (token instanceof Tokenizer.Token.SymbolToken) {
                // If there is nothing on the stack, then this symbol is the whole PST. Return it.
                // Otherwise, append it to the parent node on the top of the stack.
                Tokenizer.Token.SymbolToken symbolToken = (Tokenizer.Token.SymbolToken) token;
                ParenthesizedSymbolTree.Symbol symbol = new ParenthesizedSymbolTree.Symbol(symbolToken.getSymbol());
                if (stack.size() == 0) {
                    return symbol;
                } else {
                    stack.peek().getChildren().add(symbol);
                }
            } else if (token instanceof Tokenizer.Token.EOFToken) {
                if (stack.size() > 0) {
                    throw new Trefoil2.TrefoilError.ParenthesizedSyntaxError("Unexpected EOF " + token);
                }
                return null;
            } else {
                throw new Trefoil2.InternalInterpreterError("Impossible token: " + token.getClass());
            }
        }
    }
}
