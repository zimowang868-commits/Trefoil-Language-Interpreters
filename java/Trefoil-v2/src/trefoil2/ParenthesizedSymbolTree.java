package trefoil2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import parser.PSTParser;

import java.util.List;

/**
 * Represents a parenthesized symbol tree (PST). (Known elsewhere as an S-expression.)
 *
 * A PST is either a symbol (any string without spaces, parens, or semicolons) or a node.
 * Nodes have a (possibly empty!) list of children PSTs.
 */
public abstract class ParenthesizedSymbolTree {
    public static ParenthesizedSymbolTree parseString(String s) {
        return PSTParser.parseString(s).parse();
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class Symbol extends ParenthesizedSymbolTree {
        private final String symbol;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class Node extends ParenthesizedSymbolTree {
        private final List<ParenthesizedSymbolTree> children;
    }
}
