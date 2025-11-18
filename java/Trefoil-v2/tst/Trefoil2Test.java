import org.junit.Test;
import trefoil2.*;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class Trefoil2Test {
    // ---------------------------------------------------------------------------------------------
    // Expression tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIntLit() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("3")));
    }

    @Test
    public void testIntLitNegative() {
        assertEquals(Expression.ofInt(-10),
                Interpreter.interpretExpression(Expression.parseString("-10")));
    }

    @Test
    public void testBoolLitTrue() {
        assertEquals(new Expression.BooleanLiteral(true),
                Interpreter.interpretExpression(Expression.parseString("true")));
    }

    @Test
    public void testBoolLitFalseParsing() {
        assertEquals(Expression.ofBoolean(false), Expression.parseString("false"));
    }

    @Test
    public void testBoolLitFalse() {
        assertEquals(new Expression.BooleanLiteral(false),
                Interpreter.interpretExpression(Expression.parseString("false")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testEmptyParen() {
        Interpreter.interpretExpression(Expression.parseString("()"));
    }

    @Test
    public void testVar() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("x"),
                        Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3))));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testVarNotFound() {
        Interpreter.interpretExpression(Expression.parseString("x"));
    }

    @Test
    public void testPlus() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ 1 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testPlusMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(+ 1)"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testPlusTypeError() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ 1 true)")));
    }

    @Test
    public void testMinus() {
        assertEquals(Expression.ofInt(-1),
                Interpreter.interpretExpression(Expression.parseString("(- 1 2)")));
    }

    @Test
    public void testTimes() {
        assertEquals(Expression.ofInt(6),
                Interpreter.interpretExpression(Expression.parseString("(* 2 3)")));
    }

    @Test
    public void testEqualsIntTrue() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(= 3 (+ 1 2))")));
    }

    @Test
    public void testEqualsIntFalse() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= 4 (+ 1 2))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testEqualsIntWrongType() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= 4 true)")));
    }

    @Test
    public void testIfTrue() {
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if true 0 1)")));
    }

    @Test
    public void testIfFalse() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(if false 0 1)")));
    }

    @Test
    public void testIfNonBool() {
        // anything not false is true
        // different from how (test ...) bindings are interpreted!!
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if 5 0 1)")));
    }

    @Test
    public void testIfNoEval() {
        // since the condition is true, the interpreter should not even look at the else branch.
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if true 0 x)")));
    }

    @Test
    public void testPutVariable() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putVariable("x", Expression.ofInt(42));

        assertEquals(Expression.ofInt(42), env.getVariable("x"));
    }

    @Test
    public void testLet() {
        assertEquals(Expression.ofInt(4),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 3)) (+ x 1))")));
    }

    @Test
    public void testLetShadow1() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 1)) (let ((x 2)) x))")));
    }

    @Test
    public void testLetShadow2() {
        assertEquals(Expression.ofInt(21),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 2)) (* (let ((x 3)) x) (+ x 5)))")));
    }

    @Test
    public void testComment() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ ;asdf asdf asdf\n1 2)")));
    }

    @Test
    public void testNil() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("nil")));
    }

    @Test
    public void testCons() {
        assertEquals(Expression.cons(Expression.ofInt(1), Expression.ofInt(2)),
                Interpreter.interpretExpression(Expression.parseString("(cons 1 2)")));
    }

    // TODO: add tests for nil? and cons? here

    @Test
    public void testCar() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(car (cons 1 2))")));
    }

    @Test
    public void testCdr() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(cdr (cons 1 2))")));
    }

    // ---------------------------------------------------------------------------------------------
    // Binding tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testVarBinding() {
        assertEquals(Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3)),
                Interpreter.interpretBinding(Binding.parseString("(define x (+ 1 2))")));
    }

    @Test
    public void testVarBindingLookup() {
        Interpreter.DynamicEnvironment env = Interpreter.interpretBinding(Binding.parseString("(define x (+ 1 2))"));

        assertEquals(Expression.ofInt(3), env.getVariable("x"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testBindingEmptyParen() {
        Interpreter.interpretBinding(Binding.parseString("()"));
    }

    @Test
    public void testTopLevelExpr() {
        // We don't test anything about the answer, since the interpreter just prints it to stdout,
        // and it would be too much work to try to capture this output for testing.
        // Instead, we just check that the environment is unchanged.
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        assertEquals(env, Interpreter.interpretBinding(Binding.parseString("(* 2 x)"), env));
    }

    @Test
    public void testTestBindingPass() {
        // Who tests the tests??
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));

        // just check that no exception is thrown here
        Interpreter.interpretBinding(Binding.parseString("(test (= 3 x))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testTestBindingFail() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        Interpreter.interpretBinding(Binding.parseString("(test (= 2 x))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testTestBindingBadData() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        Interpreter.interpretBinding(Binding.parseString("(test x)"), env);
    }

    @Test
    public void testFunctionBinding() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f x) (+ x 1))"));
        env = Interpreter.interpretBinding(
                Binding.parseString("(define y (f 2))"),
                env
        );
        assertEquals(
                Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("y"), env)
        );
    }

    @Test
    public void testFunctionBindingLexicalScope() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f y) (+ x y))"),
                        Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(1))
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define z (let ((x 2)) (f 3)))"),
                env
        );
        assertEquals(
                Expression.ofInt(4),
                Interpreter.interpretExpression(Expression.parseString("z"), env)
        );
    }

    @Test
    public void testFunctionBindingRecursive() {
        String program =
                "(define (pow base exp) " +
                        "(if (= exp 0) " +
                        "    1 " +
                        "    (* base (pow base (- exp 1)))))";
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(program)
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (pow 2 3))"),
                env
        );
        assertEquals(
                Expression.ofInt(8),
                Interpreter.interpretExpression(Expression.parseString("x"), env)
        );
    }

    public static String countdownBinding =
            "(define (countdown n) " +
                    "(if (= n 0) " +
                    "    nil " +
                    "    (cons n (countdown (- n 1)))))";
    @Test
    public void testFunctionBindingListGenerator() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(countdownBinding)
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (car (cdr (countdown 10))))"),
                env
        );
        Expression ans = Interpreter.interpretExpression(Expression.parseString("x"), env);
        assertEquals(Expression.ofInt(9), ans);
    }

    @Test
    public void testFunctionBindingListConsumer() {
        String sumBinding =
                "(define (sum l) " +
                        "(if (nil? l) " +
                        "    0 " +
                        "    (+ (car l) (sum (cdr l)))))";
        System.out.println(sumBinding);
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env = Interpreter.interpretBinding(Binding.parseString(countdownBinding), env);
        env = Interpreter.interpretBinding(Binding.parseString(sumBinding), env);
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (sum (countdown 10)))"),
                env
        );
        assertEquals(
                Expression.ofInt(55),
                Interpreter.interpretExpression(Expression.parseString("x"), env)
        );
    }

    // TODO: add a test for your new top-level "test" binding here
    @Test
    public void combineSumSubTimes1() {
        assertEquals(Expression.ofInt(-9),
                Interpreter.interpretExpression(Expression.parseString("(* (- 1 2) 9)")));
    }

    @Test
    public void combineSumSubTimes2() {
        assertEquals(Expression.ofInt(-7),
                Interpreter.interpretExpression(Expression.parseString("( + (* (- 1 2) 9) 2)")));
    }

    @Test
    public void combineSumSubTime3() {
        assertEquals(Expression.ofInt(5),
                Interpreter.interpretExpression(Expression.parseString("( + (* (- 3 2) 0) 5)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void combineSumSubTimes4() {
        Interpreter.interpretExpression(Expression.parseString("( + * (- 1 2) 9) 2)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void combineSumSubTimes5() {
        Interpreter.interpretExpression(Expression.parseString("( + 3 * 4 - 5)"));
    }

    @Test
    public void Equals1() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(= 0 (- 1 1))")));
    }

    @Test
    public void Equals2() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(= 0 (* (- 2 2) 6))")));
    }

    @Test
    public void Equals3() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= 4 (* (+ 1 2) 4))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Equals4() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= 45 false)")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Equals5() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= ( = 25 true) false)")));
    }

    @Test
    public void If1() {
        assertEquals(Expression.ofInt(15),
                Interpreter.interpretExpression(Expression.parseString("(if true (* 5 3) 8)")));
    }

    @Test
    public void If2() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(if false true 1)")));
    }

    @Test
    public void If3() {
        assertEquals(Expression.ofInt(21),
                Interpreter.interpretExpression(Expression.parseString("(if false (- 4 5) (* 3 7))")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void If4() {
        Interpreter.interpretExpression(Expression.parseString("(if boom!)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void If5() {
        Interpreter.interpretExpression(Expression.parseString("(if Nothing false)"));
    }

    @Test
    public void Nil1() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("nil nil")));
    }

    @Test
    public void Nil2() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("nil Nil niiiiiil")));
    }

    @Test
    public void Nil3() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("nil (+ 1 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Nil4() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("n i l")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Nil5() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("no nil")));
    }

    @Test
    public void Cons1() {
        assertEquals(Expression.cons(Expression.ofInt(3232), Expression.ofInt(-999888)),
                Interpreter.interpretExpression(Expression.parseString("(cons 3232 -999888)")));
    }

    @Test
    public void Cons2() {
        assertEquals(Expression.cons(Expression.ofInt(-2), Expression.ofInt(48)),
                Interpreter.interpretExpression(Expression.parseString("(cons (- 3 5) (* 6 8))")));
    }

    @Test
    public void Cons3() {
        assertEquals(Expression.cons(Expression.ofInt(-6), Expression.ofInt(20)),
                Interpreter.interpretExpression(Expression.parseString("(cons (* (- 3 5) 3) (* 4 5))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Cons4() {
        Interpreter.interpretExpression(Expression.parseString("(cons a b)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void Cons5() {
        Interpreter.interpretExpression(Expression.parseString("(cons)"));
    }

    @Test
    public void Cdr1() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(cdr (cons (* 4 5) 2))")));
    }

    @Test
    public void Cdr2() {
        assertEquals(Expression.ofInt(20),
                Interpreter.interpretExpression(Expression.parseString("(cdr (cons 2 (* 4 5)))")));
    }

    @Test
    public void Cdr3() {
        assertEquals(Expression.ofInt(10),
                Interpreter.interpretExpression(Expression.parseString("(cdr (cons (* (- 2 3) 4) (+ 4 6)))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Cdr4() {
        Interpreter.interpretExpression(Expression.parseString("(cdr (cons c d))"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Cdr5() {
        Interpreter.interpretExpression(Expression.parseString("(cdr e)"));
    }

    @Test
    public void Car1() {
        assertEquals(Expression.ofInt(20),
                Interpreter.interpretExpression(Expression.parseString("(car (cons (* 4 5) 2))")));
    }

    @Test
    public void Car2() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(car (cons 2 (* 4 5)))")));
    }

    @Test
    public void Car3() {
        assertEquals(Expression.ofInt(-4),
                Interpreter.interpretExpression(Expression.parseString("(car (cons (* (- 2 3) 4) (+ 4 6)))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Car4() {
        Interpreter.interpretExpression(Expression.parseString("(car (cons a b))"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Car5() {
        Interpreter.interpretExpression(Expression.parseString("(car a)"));
    }

    @Test
    public void Let1() {
        assertEquals(Expression.ofInt(87),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 1)) (+ 58 29))")));
    }

    @Test
    public void Let2() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 898)) true)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void Let3() {
        Interpreter.interpretExpression(Expression.parseString("(let (x 98989) (+ 58 29))"));
    }


    @Test
    public void bindingTest1() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(9));
        Interpreter.interpretBinding(Binding.parseString("(test (= (* 3 3) x))"), env);
    }

    @Test
    public void bindingTest2() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("y", Expression.ofInt(9));
        Interpreter.interpretBinding(Binding.parseString("(test (= y (- (* 2 5) 1)))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void bindingTest3() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("z", Expression.ofInt(9));
        Interpreter.interpretBinding(Binding.parseString("(test (7 8))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void bindingTest4() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("z", Expression.ofInt(9));
        Interpreter.interpretBinding(Binding.parseString("(test (= x 9))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void bindingTest5() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("z", Expression.ofInt(9));
        Interpreter.interpretBinding(Binding.parseString("(test (= z (+ 4 4)))"), env);
    }

    @Test
    public void combineNilAndCons1() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(nil? (cons? nil))")));
    }

    @Test
    public void combineNilAndCons2() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(cons? (nil? nil))")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void combineNilAndCons3() {
        Interpreter.interpretExpression(Expression.parseString("(cons? (nil? nil) (nil? nil))"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void combineNilAndCons4() {
        Interpreter.interpretExpression(Expression.parseString("(nil? (cons? nil) (cons? nil))"));
    }

    @Test
    public void combineGetAndPutVariable1() {
        Interpreter.DynamicEnvironment env =
                Interpreter.DynamicEnvironment.singleton("x",
                        new Expression.Times(Expression.ofInt(5), Expression.ofInt(6)));
        env = env.extendVariable("y",
                new Expression.Minus(Expression.ofInt(10), Expression.ofInt(5)));
        assertEquals(new Expression.Minus(Expression.ofInt(10), Expression.ofInt(5)),
                env.getVariable("y"));
        assertEquals(new Expression.Times(Expression.ofInt(5), Expression.ofInt(6)),
                env.getVariable("x"));
    }

    @Test
    public void combineGetAndPutVariable2() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putVariable("y", Expression.ofInt(2));
        assertEquals(Expression.ofInt(2), env.getVariable("y"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void combineGetAndPutVariable3() {
        Interpreter.DynamicEnvironment e = Interpreter.DynamicEnvironment.empty();
        e.getVariable("y");
    }

    @Test
    public void functionBindings1() {
        Interpreter.DynamicEnvironment e = Interpreter.DynamicEnvironment.empty();
        List<String> l = new ArrayList<>();
        l.add("x");
        e = e.extendFunction("f", new Binding.FunctionBinding("f", l, new Expression.Plus(
                new Expression.VariableReference("x"), Expression.ofInt(5))));

        assertEquals(new Binding.FunctionBinding("f", l, new Expression.Plus(
                        new Expression.VariableReference("x"), Expression.ofInt(5))),
                e.getFunction("f").getFunctionBinding());
        assertEquals(e,
                e.getFunction("f").getDefiningEnvironment());
    }

    @Test
    public void functionBindings2() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        List<String> l = new ArrayList<>();
        l.add("x");
        env.putFunction("f", new Binding.FunctionBinding("f", l, new Expression.Plus(
                new Expression.VariableReference("x"), Expression.ofInt(5))));
        assertEquals(new Binding.FunctionBinding("f", l, new Expression.Plus(
                new Expression.VariableReference("x"), Expression.ofInt(5))),
                env.getFunction("f").getFunctionBinding());
        assertEquals(env,
                env.getFunction("f").getDefiningEnvironment());
    }

    @Test
    public void functionBindings3() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        List<String> l = new ArrayList<>();
        List<String> l1 = new ArrayList<>();
        l.add("x");
        l.add("y");

        env.putFunction("f", new Binding.FunctionBinding("f", l, new Expression.Plus(
                new Expression.VariableReference("f"), Expression.ofInt(2))));
        env.putFunction("something", new Binding.FunctionBinding("something", l1, new Expression.Plus(
                new Expression.VariableReference("f"), Expression.ofInt(2))));
        assertEquals(new Binding.FunctionBinding("f", l, new Expression.Plus(
                        new Expression.VariableReference("f"), Expression.ofInt(2))),
                env.getFunction("f").getFunctionBinding());
        assertEquals(new Binding.FunctionBinding("something", l1, new Expression.Plus(
                        new Expression.VariableReference("f"), Expression.ofInt(2))),
                env.getFunction("something").getFunctionBinding());
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void functionBindings4() {
        Interpreter.DynamicEnvironment e = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(10));
        Interpreter.interpretBinding(Binding.parseString("(test)"), e);
    }

    @Test
    public void functionCalls1() {
        Interpreter.DynamicEnvironment e =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f x) (* x 6))"));
        e = Interpreter.interpretBinding(
                Binding.parseString("(define y (f 2))"),
                e
        );
        assertEquals(
                Expression.ofInt(12),
                Interpreter.interpretExpression(Expression.parseString("y"), e)
        );
    }

    @Test
    public void functionCalls2() {
        Interpreter.DynamicEnvironment e =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f x) (* (- (* x 6) 4) 3))"));
        e = Interpreter.interpretBinding(
                Binding.parseString("(define z (f 2))"),
                e
        );
        assertEquals(
                Expression.ofInt(24),
                Interpreter.interpretExpression(Expression.parseString("z"), e)
        );
    }

    @Test
    public void functionCalls3() {
        String program =
                "(define (pow base exp) " +
                        "(if (= exp 4) " +
                        "    8 " +
                        "    (* base (pow base (- exp 5)))))";
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(program)
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (pow 5 9))"),
                env
        );
        assertEquals(
                Expression.ofInt(40),
                Interpreter.interpretExpression(Expression.parseString("x"), env)
        );
    }

    // Tests for my own feature.
    @Test
    public void Divide1() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(rem 1 2)")));
    }

    @Test
    public void Divide2() {
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(rem 8 2)")));
    }

    @Test
    public void Divide3() {
        assertEquals(Expression.ofInt(-20),
                Interpreter.interpretExpression(Expression.parseString("(* (- (rem 8 2) 4) 5)")));
    }

    @Test
    public void Divide4() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(- (rem (+ (rem 8 2) 5) 387) 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void Divide5() {
        Interpreter.interpretExpression(Expression.parseString("(rem 4 0)"));
    }
}
