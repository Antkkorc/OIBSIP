package bw.oibsip.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Expression maths for the calculator, with no Android imports so it can be tested on
 * the JVM. Precedence is handled in two passes: × and ÷ first, then + and −.
 *
 * The tape is a List<String> alternating number, operator, number, ... which makes
 * malformed input impossible to hand to the evaluator by accident.
 */
public final class Evaluator {

    public static final String PLUS = "+";
    public static final String MINUS = "\u2212";
    public static final String TIMES = "\u00D7";
    public static final String DIVIDE = "\u00F7";

    public static final String DIVIDE_BY_ZERO = "DIVIDE BY ZERO";
    public static final String OUT_OF_RANGE = "OUT OF RANGE";

    private static final DecimalFormat GROUPED =
            new DecimalFormat("#,##0.##########", DecimalFormatSymbols.getInstance(Locale.US));

    static {
        GROUPED.setRoundingMode(RoundingMode.HALF_UP);
    }

    private Evaluator() {
    }

    public static boolean isOperator(String token) {
        return PLUS.equals(token) || MINUS.equals(token)
                || TIMES.equals(token) || DIVIDE.equals(token);
    }

    /**
     * @throws ArithmeticException on division by zero or on a result that overflows
     * @throws IllegalArgumentException when the token list is not a valid chain
     */
    public static double evaluate(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0d;
        }
        if (tokens.size() % 2 == 0) {
            throw new IllegalArgumentException("Incomplete expression");
        }

        List<Double> numbers = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (i % 2 == 0) {
                numbers.add(number(token));
            } else {
                if (!isOperator(token)) {
                    throw new IllegalArgumentException("Expected an operator: " + token);
                }
                operators.add(token);
            }
        }
        if (numbers.size() != operators.size() + 1) {
            throw new IllegalArgumentException("Incomplete expression");
        }

        // Pass one: × and ÷ fold into the number on their left.
        List<Double> folded = new ArrayList<>();
        List<String> additions = new ArrayList<>();
        folded.add(numbers.get(0));
        for (int i = 0; i < operators.size(); i++) {
            String operator = operators.get(i);
            double next = numbers.get(i + 1);
            int last = folded.size() - 1;
            if (TIMES.equals(operator)) {
                folded.set(last, folded.get(last) * next);
            } else if (DIVIDE.equals(operator)) {
                if (next == 0d) {
                    throw new ArithmeticException(DIVIDE_BY_ZERO);
                }
                folded.set(last, folded.get(last) / next);
            } else {
                additions.add(operator);
                folded.add(next);
            }
        }

        // Pass two: + and −, left to right.
        double total = folded.get(0);
        for (int i = 0; i < additions.size(); i++) {
            double next = folded.get(i + 1);
            if (PLUS.equals(additions.get(i))) {
                total += next;
            } else {
                total -= next;
            }
        }

        if (Double.isNaN(total) || Double.isInfinite(total)) {
            throw new ArithmeticException(OUT_OF_RANGE);
        }
        return total;
    }

    private static double number(String token) {
        String cleaned = token.replace(",", "").replace("\u2212", "-");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: " + token);
        }
    }

    /** Readout formatting: grouping, up to ten decimals, scientific at the extremes. */
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "ERROR";
        }
        double abs = Math.abs(value);
        if (abs < 5e-12d) {
            return "0";
        }
        if (abs >= 1e12d || abs < 1e-9d) {
            return String.format(Locale.US, "%.6E", value);
        }
        return GROUPED.format(value);
    }

    /** Inserts grouping separators into the integer part of a half-typed number. */
    public static String group(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "0";
        }
        boolean negative = raw.startsWith("-");
        String body = negative ? raw.substring(1) : raw;
        int dot = body.indexOf('.');
        String integer = dot < 0 ? body : body.substring(0, dot);
        String fraction = dot < 0 ? "" : body.substring(dot);
        if (integer.length() <= 3) {
            return (negative ? "-" : "") + integer + fraction;
        }
        StringBuilder grouped = new StringBuilder();
        int count = 0;
        for (int i = integer.length() - 1; i >= 0; i--) {
            grouped.append(integer.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) {
                grouped.append(',');
            }
        }
        return (negative ? "-" : "") + grouped.reverse() + fraction;
    }

    /** A compact, machine-readable number string for chaining the next operation. */
    public static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /**
     * Turns typed text into the token chain the evaluator expects. Accepts ASCII
     * fallbacks (* and /) and a sign after an operator, e.g. "5×-2".
     */
    public static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        if (expression == null) {
            return tokens;
        }
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                current.append(c);
                continue;
            }
            if (c == ' ' || c == '\t') {
                continue;
            }
            boolean signAfterOperator = (c == '-' || c == '+')
                    && current.length() == 0
                    && (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1)));
            if (signAfterOperator) {
                if (c == '-') {
                    current.append('-');
                }
                continue;
            }
            if (current.length() > 0) {
                tokens.add(current.toString());
                current.setLength(0);
            }
            if (c == '+') {
                tokens.add(PLUS);
            } else if (c == '-' || c == '\u2212') {
                tokens.add(MINUS);
            } else if (c == '*' || c == 'x' || c == '\u00D7') {
                tokens.add(TIMES);
            } else if (c == '/' || c == '\u00F7') {
                tokens.add(DIVIDE);
            } else {
                throw new IllegalArgumentException("Unknown character: " + c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
