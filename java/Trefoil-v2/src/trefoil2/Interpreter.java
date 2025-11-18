package trefoil2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets expressions and bindings in the context of a dynamic environment
 * according to the semantics of Trefoil v2.
 */
public class Interpreter {
    /**
     * Evaluates e in the given environment. Returns the resulting value.
     *
     * Throws TrefoilError.RuntimeError when the Trefoil programmer makes a mistake.
     */
    public static Expression interpretExpression(Expression e, DynamicEnvironment environment) {
        if (e instanceof Expression.IntegerLiteral || e instanceof Expression.BooleanLiteral) {
            return e;
        } else if (e instanceof Expression.VariableReference) {
            Expression.VariableReference var = (Expression.VariableReference) e;
            return environment.getVariable(var.getVarname());
        } else if (e instanceof Expression.Plus) {
            Expression.Plus p = (Expression.Plus) e;
            Expression v1 = interpretExpression(p.getLeft(), environment);
            Expression v2 = interpretExpression(p.getRight(), environment);
            // TODO: the following return statement is wrong because it does not correctly check
            //       for run-time type errors. fix it by checking that both children evaluated to
            //       IntegerLiterals and if not throwing TrefoilError.RuntimeError.

            if (!(v1 instanceof Expression.IntegerLiteral && v2 instanceof  Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("Both of expression should be type of integer literals");
            }
            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() +
                            ((Expression.IntegerLiteral) v2).getData()
            );

        // TODO: implement semantics for new AST nodes here, following the examples above
        // TODO: be sure to check for run-time type errors and throw TrefoilError.RuntimeError.
        } else if (e instanceof Expression.Minus) {
            Expression.Minus m = (Expression.Minus) e;
            Expression v1 = interpretExpression(m.getLeft(), environment);
            Expression v2 = interpretExpression(m.getRight(), environment);

            if (!(v1 instanceof Expression.IntegerLiteral && v2 instanceof  Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("Both of expression should be type of integer literals");
            }
            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() -
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else if (e instanceof Expression.Times) {
            Expression.Times t = (Expression.Times) e;
            Expression v1 = interpretExpression(t.getLeft(), environment);
            Expression v2 = interpretExpression(t.getRight(), environment);

            if (!(v1 instanceof Expression.IntegerLiteral && v2 instanceof Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("Both of expression should be type of integer literals");
            }
            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() *
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else if (e instanceof Expression.Equals) {
            Expression.Equals e1 = (Expression.Equals) e;
            Expression v1 = interpretExpression(e1.getLeft(), environment);
            Expression v2 = interpretExpression(e1.getRight(), environment);

            if (!(v1 instanceof Expression.IntegerLiteral && v2 instanceof Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("Both of expression should be type of integer literals");
            }
            return new Expression.BooleanLiteral(
                    ((Expression.IntegerLiteral) v1).getData() ==
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else if (e instanceof Expression.If) {
            Expression.If e1 = (Expression.If) e;
            Expression v1 = interpretExpression(e1.getLeft(), environment);

            if (v1 instanceof Expression.BooleanLiteral &&
                    !((Expression.BooleanLiteral) interpretExpression(v1)).isData()) {
                return interpretExpression(interpretExpression(e1.getRight(), environment));
            } else {
                return interpretExpression(interpretExpression(e1.getMiddle(), environment));
            }
        } else if (e instanceof Expression.Cons) {
            Expression.Cons c = (Expression.Cons) e;
            Expression v1 = interpretExpression(c.getLeft(), environment);
            Expression v2 = interpretExpression(c.getRight(), environment);

            return new Expression.Cons(v1, v2);
        } else if (e instanceof Expression.Nil) {
            return new Expression.Nil();
        } else if (e instanceof Expression.isNil) {
            Expression.isNil i = (Expression.isNil) e;
            Expression v1 = interpretExpression(i.getLeft(), environment);

            if (v1 instanceof Expression.Nil) {
                return new Expression.BooleanLiteral(true);
            } else {
                return new Expression.BooleanLiteral(false);
            }
        } else if (e instanceof Expression.isCons) {
            Expression.isCons c1 = (Expression.isCons) e;
            Expression v = interpretExpression(c1.getLeft(), environment);

            if (v instanceof Expression.Cons) {
                return new Expression.BooleanLiteral(true);
            } else {
                return new Expression.BooleanLiteral(false);
            }
        } else if (e instanceof  Expression.Car) {
            Expression.Car ca = (Expression.Car) e;
            Expression v = interpretExpression(ca.getLeft(), environment);

            Expression.Cons c = (Expression.Cons) v;
            return interpretExpression(c.getLeft(), environment);
        } else if (e instanceof  Expression.Cdr) {
            Expression.Cdr cd = (Expression.Cdr) e;
            Expression v = interpretExpression(cd.getLeft(), environment);

            Expression.Cons c = (Expression.Cons) v;
            return interpretExpression(c.getRight(), environment);
        } else if (e instanceof Expression.Let) {
            Expression.Let l = (Expression.Let) e;
            Expression v1 = interpretExpression(l.getLeft(), environment);
            String string = l.getString();

            DynamicEnvironment env = environment.extendVariable(string, v1);
            return interpretExpression(v1, env);
        } else if (e instanceof Expression.Function) {
            DynamicEnvironment callenv = environment;
            DynamicEnvironment defenv = environment.getFunction(((Expression.Function) e).getString()).definingEnvironment;
            Binding.FunctionBinding bind = environment.getFunction(((Expression.Function) e).getString()).functionBinding;
            List<Expression> list;

            if (bind.getArgnames().size() != ((Expression.Function) e).getExpressions().size()) {
                throw new Trefoil2.TrefoilError.RuntimeError("Arguments and parameters are sequences of different lengths");
            }
            list = new ArrayList<>();
            for (int i = 0; i < ((Expression.Function) e).getExpressions().size(); i++) {
                list.add(interpretExpression(((Expression.Function) e).getExpressions().get(i), callenv));
            }
            return interpretExpression(bind.getBody(), defenv.extendVariables(bind.getArgnames(), list));

            // Interpret my own feature.
        } else if (e instanceof Expression.Remainder) {
            Expression.Remainder d = (Expression.Remainder) e;
            Expression v1 = interpretExpression(d.getLeft(), environment);
            Expression v2 = interpretExpression(d.getRight(), environment);

            if (!(v1 instanceof Expression.IntegerLiteral && v2 instanceof  Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("Both of expression should be type of integer literals");
            }
            if (((Expression.IntegerLiteral) v2).getData() == 0) {
                throw new Trefoil2.TrefoilError.RuntimeError(v1 + " can't be divided by 0");
            }
            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() %
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else {
            // Otherwise, it's an expression AST node we don't recognize. Tell the interpreter implementor.
            throw new Trefoil2.InternalInterpreterError("\"impossible\" expression AST node " + e.getClass());
        }
    }

    /**
     * Executes the binding in the given environment, returning the new environment.
     *
     * The environment passed in as an argument is *not* mutated. Instead, it is copied
     * and any modifications are made on the copy and returned.
     *
     * Throws TrefoilError.RuntimeError when the Trefoil programmer makes a mistake.
     */
    public static DynamicEnvironment interpretBinding(Binding b, DynamicEnvironment environment) {
        if (b instanceof Binding.VariableBinding) {
            Binding.VariableBinding vb = (Binding.VariableBinding) b;
            Expression value = interpretExpression(vb.getVardef(), environment);
            System.out.println(vb.getVarname() + " = " + value);
            return environment.extendVariable(vb.getVarname(), value);
        } else if (b instanceof Binding.TopLevelExpression) {
            Binding.TopLevelExpression tle = (Binding.TopLevelExpression) b;
            System.out.println(interpretExpression(tle.getExpression(), environment));
            return environment;
        } else if (b instanceof Binding.FunctionBinding) {
            Binding.FunctionBinding fb = (Binding.FunctionBinding) b;
            DynamicEnvironment newEnvironment = environment.extendFunction(fb.getFunname(), fb);
            System.out.println(fb.getFunname() + " is defined");
            return newEnvironment;
        // TODO: implement the TestBinding here
        } else if (b instanceof Binding.TestBinding) {
            Binding.TestBinding tb = (Binding.TestBinding) b;
            Expression value = interpretExpression(tb.getExpression(), environment);

            if(value instanceof Expression.BooleanLiteral) {
                if (((Expression.BooleanLiteral) value).isData()) {
                    return environment;
                }
            }
            throw new Trefoil2.TrefoilError.RuntimeError("Two expressions are not equal");
        }

        // Otherwise it's a binding AST node we don't recognize. Tell the interpreter implementor.
        throw new Trefoil2.InternalInterpreterError("\"impossible\" binding AST node " + b.getClass());
    }


    // Convenience methods for interpreting in the empty environment.
    // Used for testing.
    public static Expression interpretExpression(Expression e) {
        return interpretExpression(e, new DynamicEnvironment());
    }
    public static DynamicEnvironment interpretBinding(Binding b) {
        return interpretBinding(b, DynamicEnvironment.empty());
    }


    /**
     * Represents the dynamic environment, which is a mapping from strings to "entries".
     * In the starter code, the string always represents a variable name and an entry is always a VariableEntry.
     * You will extend it to also support function names and FunctionEntries.
     */
    @Data
    public static class DynamicEnvironment {
        public static abstract class Entry {
            @EqualsAndHashCode(callSuper = false)
            @Data
            public static class VariableEntry extends Entry {
                private final Expression value;
            }

            @EqualsAndHashCode(callSuper = false)
            @Data
            public static class FunctionEntry extends Entry {
                private final Binding.FunctionBinding functionBinding;

                @ToString.Exclude
                private final DynamicEnvironment definingEnvironment;
            }

            // Convenience factory methods

            public static Entry variable(Expression value) {
                return new VariableEntry(value);
            }
            public static Entry function(Binding.FunctionBinding functionBinding, DynamicEnvironment definingEnvironment) {
                return new FunctionEntry(functionBinding, definingEnvironment);
            }
        }

        // The backing map of this dynamic environment.
        private final Map<String, Entry> map;

        public DynamicEnvironment() {
            this.map = new HashMap<>();
        }

        public DynamicEnvironment(DynamicEnvironment other) {
            this.map = new HashMap<>(other.getMap());
        }

        private boolean containsVariable(String varname) {
            return map.containsKey(varname) && map.get(varname) instanceof Entry.VariableEntry;
        }

        public Expression getVariable(String varname) {
            // TODO: convert this assert to instead throw a TrefoilError.RuntimeError if the variable is not bound
            if (!containsVariable(varname)) {
                throw new Trefoil2.TrefoilError.RuntimeError("The variable is not bound");
            }

            // TODO: lookup the variable in the map and return the corresponding value
            // Hint: first, read the code for containsVariable().
            // Hint: you will likely need the value field from Entry.VariableEntry
            return ((Entry.VariableEntry) map.get(varname)).getValue();
        }

        public void putVariable(String varname, Expression value) {
            // TODO: bind the variable in the backing map
            // Hint: map.put
            // Hint: either call new Entry.VariableEntry or the factory Entry.variable
            Expression e = Interpreter.interpretExpression(value, this);
            System.out.println(varname + " = " + e.toString());
            map.put(varname, Entry.variable(value));
        }

        /**
         * Returns a *new* DynamicEnvironment extended by the binding varname -> value.
         *
         * Does not change this! Creates a copy.
         */
        public DynamicEnvironment extendVariable(String varname, Expression value) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);  // create a copy
            newEnv.putVariable(varname, value);  // mutate the copy
            return newEnv;  // return the mutated copy (this remains unchanged!)
        }

        /**
         * Returns a *new* Dynamic environment extended by the given mappings.
         *
         * Does not change this! Creates a copy.
         *
         * varnames and values must have the same length
         *
         * @param varnames variable names to bind
         * @param values values to bind the variables to
         */
        public DynamicEnvironment extendVariables(List<String> varnames, List<Expression> values) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);
            assert varnames.size() == values.size();
            for (int i = 0; i < varnames.size(); i++) {
                newEnv.putVariable(varnames.get(i), values.get(i));
            }
            return newEnv;
        }

        private boolean containsFunction(String funname) {
            return map.containsKey(funname) && map.get(funname) instanceof Entry.FunctionEntry;
        }

        public Entry.FunctionEntry getFunction(String funname) {
            // TODO: convert this assert to instead throw a TrefoilError.RuntimeError if the function is not bound
            if (containsVariable(funname)) {
                throw new Trefoil2.TrefoilError.RuntimeError("The variable doesn't contain");
            }

            // TODO: lookup the function in the map and return the corresponding function binding
            // Hint: first, read the code for containsFunction().
            return ((Entry.FunctionEntry) map.get(funname));
        }

        public void putFunction(String funname, Binding.FunctionBinding functionBinding) {
            // TODO: bind the function in the backing map
            // Be careful to set up recursion correctly!
            // Hint: Pass definingEnvironment=this to the Entry.function factory, and then call map.put.
            //       That way, by the time Trefoil calls the function, everything points to
            //       the right place. Tricky!
            List<String> l = functionBinding.getArgnames();
            for (int i = 0; i < l.size(); i++) {
                for (int j = i + 1; j < l.size(); j++) {
                    if (l.get(i).equals(l.get(j))) {
                        throw new Trefoil2.TrefoilError.RuntimeError(l.get(i) + " is same in both list");
                    }
                }
            }
            map.put(funname, Entry.function(functionBinding, this));
        }

        public DynamicEnvironment extendFunction(String funname, Binding.FunctionBinding functionBinding) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);  // create a copy of this
            newEnv.putFunction(funname, functionBinding);  // mutate the copy
            return newEnv;  // return the copy
        }

        // Convenience factory methods

        public static DynamicEnvironment empty() {
            return new DynamicEnvironment();
        }

        public static DynamicEnvironment singleton(String varname, Expression value) {
            return empty().extendVariable(varname, value);
        }
    }
}
