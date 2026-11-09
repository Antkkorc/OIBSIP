package bw.oibsip.calc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EvaluatorTest {

    private static List<String> tape(String expression) {
        return Evaluator.tokenize(expression);
    }

    @Test
    public void multiplicationBindsTighterThanAddition() {
        assertEquals(14d, Evaluator.evaluate(tape("2+3*4")), 1e-9);
        assertEquals(20d, Evaluator.evaluate(tape("4*3+8")), 1e-9);
        assertEquals(9d, Evaluator.evaluate(tape("10-6/3+1")), 1e-9);
    }

    @Test
    public void handlesDecimalsAndAsciiFallbacks() {
        assertEquals(2.5d, Evaluator.evaluate(tape("10/4")), 1e-9);
        assertEquals(1.5d, Evaluator.evaluate(tape("0.5+1")), 1e-9);
        assertEquals(6d, Evaluator.evaluate(tape("2x3")), 1e-9);
    }

    @Test
    public void leftToRightWithinTheSamePrecedence() {
        assertEquals(1d, Evaluator.evaluate(tape("10-6-3")), 1e-9);
        assertEquals(1d, Evaluator.evaluate(tape("100/10/10")), 1e-9);
    }

    @Test
    public void divisionByZeroThrowsWithAReason() {
        try {
            Evaluator.evaluate(tape("8/0"));
            fail("Division by zero should throw");
        } catch (ArithmeticException expected) {
            assertEquals(Evaluator.DIVIDE_BY_ZERO, expected.getMessage());
        }
    }

    @Test
    public void malformedTapeThrowsIllegalArgument() {
        try {
            Evaluator.evaluate(Arrays.asList("2", "+"));
            fail("An incomplete tape must not evaluate");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Incomplete"));
        }
        try {
            Evaluator.evaluate(Arrays.asList("2", "%", "3"));
            fail("An unknown operator must not evaluate");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("operator"));
        }
    }

    @Test
    public void signAfterOperatorIsPartOfTheNumber() {
        List<String> tokens = tape("5*-2");
        assertEquals(3, tokens.size());
        assertEquals(-10d, Evaluator.evaluate(tokens), 1e-9);
    }

    @Test
    public void formatsForTheReadout() {
        assertEquals("0.3", Evaluator.format(0.1d + 0.2d));
        assertEquals("0.3333333333", Evaluator.format(1d / 3d));
        assertEquals("14", Evaluator.format(14d));
        assertEquals("1,234,567.5", Evaluator.format(1234567.5d));
        assertEquals("0", Evaluator.format(0d));
        assertEquals("1.000000E+13", Evaluator.format(1e13d));
    }

    @Test
    public void groupsTheIntegerPartOnly() {
        assertEquals("1,234", Evaluator.group("1234"));
        assertEquals("1,234.5", Evaluator.group("1234.5"));
        assertEquals("12.5", Evaluator.group("12.5"));
        assertEquals("-1,000,000", Evaluator.group("-1000000"));
        assertEquals("0", Evaluator.group(""));
        assertEquals("1,234.", Evaluator.group("1234."));
    }

    @Test
    public void plainNumbersChainBackIntoTheTape() {
        assertEquals("0.125", Evaluator.plain(0.125d));
        assertEquals("12", Evaluator.plain(12.000d));
        assertEquals("-3", Evaluator.plain(-3d));
    }
}
