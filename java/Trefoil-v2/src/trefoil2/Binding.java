
package trefoil2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Test;
import parser.Tokenizer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AST of a top level binding in a Trefoil v2 program.
 *
 * See LANGUAGE.md for a list of possibilities.
 */
@Data
public abstract class Binding {
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class VariableBinding extends Binding {
        private final String varname;
        private final Expression vardef;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TopLevelExpression extends Binding {
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FunctionBinding extends Binding {
        private final String funname;
        private final List<String> argnames;
        private final Expression body;
    }

    // TODO: define a new kind of binding called TestBinding that takes an expression

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TestBinding extends Binding {
        private final Expression expression;
    }


    /**
     * Tries to convert a PST to a Binding.
     *
     * See LANGUAGE.md for a description of how this should work at a high level.
     *
     * If conversion fails, throws TrefoilError.AbstractSyntaxError with a nice message.
     */
    public static Binding parsePST(ParenthesizedSymbolTree pst) {
        // The pst is either a symbol or a node.
        // The only way a symbol can be a binding is as a top-level expression.
        if (pst instanceof ParenthesizedSymbolTree.Symbol) {
            return new TopLevelExpression(Expression.parsePST(pst));
        }
        // Now we know the PST is a Node.
        ParenthesizedSymbolTree.Node n = (ParenthesizedSymbolTree.Node) pst;
        List<ParenthesizedSymbolTree> children = n.getChildren();
        if (children.size() == 0) {
            throw new Trefoil2.TrefoilError.AbstractSyntaxError("Unexpected empty pair of parentheses.");
        }
        String head = ((ParenthesizedSymbolTree.Symbol) children.get(0)).getSymbol();
        if (head.equals("define")) {
            // If the head is define, it's either a variable binding or a function binding.
            if (children.size() - 1 != 2) {
                throw new Trefoil2.TrefoilError.AbstractSyntaxError("Top-level define expects two arguments.");
            }
            if (children.get(1) instanceof ParenthesizedSymbolTree.Symbol) {
                ParenthesizedSymbolTree.Symbol varsym = (ParenthesizedSymbolTree.Symbol) children.get(1);
                return new VariableBinding(varsym.getSymbol(), Expression.parsePST(children.get(2)));
            } else {
                ParenthesizedSymbolTree.Node node = (ParenthesizedSymbolTree.Node) children.get(1);
                List<ParenthesizedSymbolTree> funAndArgs = node.getChildren();
                if (!funAndArgs.stream().allMatch(x -> x instanceof ParenthesizedSymbolTree.Symbol) ||
                        funAndArgs.size() < 1) {
                    throw new Trefoil2.TrefoilError.AbstractSyntaxError("Malformed top-level function binding");
                }

                return new FunctionBinding(((ParenthesizedSymbolTree.Symbol) funAndArgs.get(0)).getSymbol(),
                        funAndArgs.subList(1, funAndArgs.size()).stream()
                                .map(x -> ((ParenthesizedSymbolTree.Symbol) x).getSymbol())
                                .collect(Collectors.toList()),
                        Expression.parsePST(children.get(2)));
            }
        // TODO: uncomment when ready to implement test binding
        } else if (head.equals("test")) {
            // TODO: parse test binding here
            // Hint: Check that there is exactly one child, then use trefoil2.Expression.parsePST on the child.
            //       Remember that the children list includes the head, so the real children start at
            //       index 1.
            if (children.size() - 1 == 1) {
                Expression e = Expression.parsePST(children.get(1));
                return new TestBinding(e);
            }
            throw new Trefoil2.TrefoilError.AbstractSyntaxError("Children size should be 2");
        } else {
            // If the head is not recognized, the whole PST represents a top-level expression.
            return new TopLevelExpression(Expression.parsePST(pst));
        }
    }

    // Convenience factory method for unit testsing.
    public static Binding parseString(String s) {
        return parsePST(ParenthesizedSymbolTree.parseString(s));
    }
}