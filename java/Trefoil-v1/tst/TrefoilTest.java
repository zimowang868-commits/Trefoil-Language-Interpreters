import org.junit.Test;

import static junit.framework.TestCase.*;

public class TrefoilTest {
    @Test
    public void interpretOne() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("1");
        assertEquals(1, trefoil.pop());
    }

    @Test
    public void interpretAdd() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("1 2 +");
        assertEquals(3, trefoil.pop());
    }

    @Test
    public void interpretAddSplit() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("1 2");
        // the interpreter should track the stack across multiple calls to interpret()
        trefoil.interpret("+");
        assertEquals(3, trefoil.pop());
    }

    @Test
    public void toString_() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("1 2 3");
        assertEquals("1 2 3", trefoil.toString());
    }

    // TODO: add unit tests here to cover all features in the language (don't forget to test comments!)
    @Test
    public void interpretMoreCases() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("2 3 *");
        assertEquals(6, trefoil.pop());

        trefoil.interpret("8 4 -");
        assertEquals(4, trefoil.pop());

        trefoil.interpret("; 1 2 3");
        assertEquals("1 2 3", trefoil.toString());

        trefoil.interpret("; 1");
        assertEquals("1", trefoil.toString());

        trefoil.interpret("1 2 3 .");
        assertEquals("1 2", trefoil.toString());

        trefoil.interpret("1 2 3 . . ");
        assertEquals("1", trefoil.toString());

        trefoil.interpret("4 5 6 7 8 . . .");
        assertEquals("4 5", trefoil.toString());

        trefoil.interpret("4 5 6 - 7 8 9 . . .");
        assertEquals("4 -1", trefoil.toString());

        trefoil.interpret("; 3 4 + 5 6 * 7 8 9 . . .");
        assertEquals("7 30", trefoil.toString());

        trefoil.interpret("4 5 * 6 7 * 8 ; 9 . . .");
        assertEquals("20", trefoil.toString());

        trefoil.interpret("5 ; ; 4 -");
        assertEquals("1", trefoil.toString());

        trefoil.interpret("5 2 + 3 . 2 4 * ; . ; 4 8 *");
        assertEquals("7 32", trefoil.toString());

        trefoil.interpret("5 ; . 8 4 * 4");
        assertEquals("32 4", trefoil.toString());

        trefoil.interpret("5 ; ; . 6 3 . 2 * 3");
        assertEquals("12 3", trefoil.toString());

        trefoil.interpret("1 2 3 4 .");
        trefoil.interpret("*");
        assertEquals("1 6", trefoil.toString());

        trefoil.interpret("2 4 6 8 10");
        trefoil.interpret(". . -");
        assertEquals("2 -2", trefoil.toString());
    }



    @Test(expected = Trefoil.TrefoilError.class)
    public void stackUnderflow() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("1 +");
    }

    // TODO: add unit tests for malformed programs that the user might accidentally input
    //       you can use the @Test(expected = Trefoil.TrefoilError.class) notation above
    //       to write a test that fails if the exception is *not* thrown. Add at least
    //       one test for each operator that can signal an error, plus at least one test
    //       containing malformed input (a word that is not a token).
    @Test(expected = Trefoil.TrefoilError.class)
    public void errorInput() {
        Trefoil trefoil = new Trefoil();
        trefoil.interpret("p");
        trefoil.interpret("; p");
        trefoil.interpret("1 2 p");
        trefoil.interpret("p 1 2 ;");
        trefoil.interpret("*");
        trefoil.interpret("-");
        trefoil.interpret(".");
        trefoil.interpret("+");
    }
}