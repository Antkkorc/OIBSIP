package bw.oibsip.calc;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Instrument 03 — Calculator.
 *
 * The tape is a token list, never a raw string, so rapid or repeated taps can only ever
 * produce a valid chain: numbers and operators alternate, and a second consecutive
 * operator replaces the first instead of corrupting the expression.
 */
public class MainActivity extends AppCompatActivity {

    private static final int MAX_TOKENS = 24;
    private static final int MAX_OPERAND = 14;

    private static final String STATE_TOKENS = "tokens";
    private static final String STATE_OPERAND = "operand";
    private static final String STATE_RESULT = "result";
    private static final String STATE_EXPRESSION = "expression";
    private static final String STATE_EVALUATED = "evaluated";
    private static final String STATE_ERROR = "error";
    private static final String STATE_REASON = "reason";

    private final List<String> tokens = new ArrayList<>();

    private String operand = "";
    private String resultRaw;
    private String lastExpression = "";
    private boolean evaluated;
    private boolean error;
    private String errorReason = "";

    private TextView tapeView;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tapeView = findViewById(R.id.tape);
        resultView = findViewById(R.id.result);

        bindKeys();
        restore(savedInstanceState);
        render();
    }

    private void bindKeys() {
        bindDigit(R.id.key0, "0");
        bindDigit(R.id.key1, "1");
        bindDigit(R.id.key2, "2");
        bindDigit(R.id.key3, "3");
        bindDigit(R.id.key4, "4");
        bindDigit(R.id.key5, "5");
        bindDigit(R.id.key6, "6");
        bindDigit(R.id.key7, "7");
        bindDigit(R.id.key8, "8");
        bindDigit(R.id.key9, "9");

        findViewById(R.id.keyDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressDot();
            }
        });
        findViewById(R.id.keyAdd).setOnClickListener(operatorListener(Evaluator.PLUS));
        findViewById(R.id.keySubtract).setOnClickListener(operatorListener(Evaluator.MINUS));
        findViewById(R.id.keyMultiply).setOnClickListener(operatorListener(Evaluator.TIMES));
        findViewById(R.id.keyDivide).setOnClickListener(operatorListener(Evaluator.DIVIDE));
        findViewById(R.id.keyEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressEquals();
            }
        });
        findViewById(R.id.keyClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressClear();
            }
        });
        findViewById(R.id.keyDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressDelete();
            }
        });
        findViewById(R.id.keySign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressSign();
            }
        });
        findViewById(R.id.keyPercent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressPercent();
            }
        });
    }

    private void bindDigit(int id, final String digit) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressDigit(digit);
            }
        });
    }

    private View.OnClickListener operatorListener(final String operator) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressOperator(operator);
            }
        };
    }

    // ---------------------------------------------------------------- key handlers

    private void pressDigit(String digit) {
        clearError();
        if (evaluated) {
            resetTape();
        }
        if (operand.length() >= MAX_OPERAND) {
            return;
        }
        if ("0".equals(operand)) {
            operand = digit;
        } else if ("-0".equals(operand)) {
            operand = "-" + digit;
        } else {
            operand = operand + digit;
        }
        render();
    }

    private void pressDot() {
        clearError();
        if (evaluated) {
            resetTape();
        }
        if (operand.contains(".") || operand.length() >= MAX_OPERAND) {
            return;
        }
        operand = operand.isEmpty() ? "0." : (operand + ".");
        render();
    }

    private void pressOperator(String operator) {
        if (error) {
            return;
        }
        if (evaluated) {
            tokens.clear();
            if (resultRaw != null) {
                tokens.add(resultRaw);
            }
            operand = "";
            lastExpression = "";
            evaluated = false;
        }
        if (!operand.isEmpty()) {
            tokens.add(operand);
            operand = "";
        }
        if (tokens.isEmpty()) {
            tokens.add("0");
        }
        if (tokens.size() >= MAX_TOKENS) {
            return;
        }
        String last = tokens.get(tokens.size() - 1);
        if (Evaluator.isOperator(last)) {
            tokens.set(tokens.size() - 1, operator);
        } else {
            tokens.add(operator);
        }
        render();
    }

    private void pressEquals() {
        if (error) {
            return;
        }
        if (!operand.isEmpty()) {
            tokens.add(operand);
            operand = "";
        }
        while (!tokens.isEmpty() && Evaluator.isOperator(tokens.get(tokens.size() - 1))) {
            tokens.remove(tokens.size() - 1);
        }
        if (tokens.isEmpty()) {
            render();
            return;
        }

        String expression = TextUtils.join(" ", tokens);
        try {
            double value = Evaluator.evaluate(tokens);
            String text = Evaluator.format(value);
            resultRaw = text.replace(",", "");
            lastExpression = expression + " =";
            tokens.clear();
            evaluated = true;
            render();
            fadeReadout();
        } catch (ArithmeticException arithmetic) {
            enterError(Evaluator.DIVIDE_BY_ZERO.equals(arithmetic.getMessage())
                    ? getString(R.string.error_divide_by_zero)
                    : getString(R.string.error_overflow));
        } catch (RuntimeException malformed) {
            // Defensive: a malformed tape must never take the app down.
            enterError(getString(R.string.error_overflow));
        }
    }

    private void pressClear() {
        resetTape();
        clearError();
        render();
    }

    private void pressDelete() {
        if (error) {
            pressClear();
            return;
        }
        if (evaluated) {
            pressClear();
            return;
        }
        if (!operand.isEmpty()) {
            operand = operand.substring(0, operand.length() - 1);
            if ("-".equals(operand)) {
                operand = "";
            }
        } else if (!tokens.isEmpty()) {
            String removed = tokens.remove(tokens.size() - 1);
            if (!Evaluator.isOperator(removed)) {
                operand = removed.length() > 1
                        ? removed.substring(0, removed.length() - 1)
                        : "";
            }
        }
        render();
    }

    private void pressSign() {
        clearError();
        if (evaluated) {
            if (resultRaw == null) {
                return;
            }
            resultRaw = resultRaw.startsWith("-")
                    ? resultRaw.substring(1)
                    : "-" + resultRaw;
            render();
            return;
        }
        if (operand.isEmpty()) {
            return;
        }
        operand = operand.startsWith("-")
                ? operand.substring(1)
                : "-" + operand;
        render();
    }

    private void pressPercent() {
        clearError();
        String source = !operand.isEmpty()
                ? operand
                : (evaluated ? resultRaw : null);
        if (source == null) {
            return;
        }
        try {
            operand = Evaluator.plain(Double.parseDouble(source) / 100d);
            evaluated = false;
            render();
        } catch (NumberFormatException ignored) {
            // Nothing sensible to divide; leave the tape untouched.
        }
    }

    // ---------------------------------------------------------------- state

    private void resetTape() {
        tokens.clear();
        operand = "";
        resultRaw = null;
        lastExpression = "";
        evaluated = false;
    }

    private void clearError() {
        if (error) {
            error = false;
            errorReason = "";
            resetTape();
        }
    }

    private void enterError(String reason) {
        error = true;
        errorReason = reason.toUpperCase(Locale.US);
        tokens.clear();
        operand = "";
        resultRaw = null;
        evaluated = false;
        lastExpression = "";
        render();
        fadeReadout();
    }

    private void render() {
        String big;
        if (error) {
            big = getString(R.string.error_text).toUpperCase(Locale.US);
        } else if (!operand.isEmpty()) {
            big = Evaluator.group(operand);
        } else if (evaluated && resultRaw != null) {
            big = Evaluator.group(resultRaw);
        } else if (!tokens.isEmpty() && !Evaluator.isOperator(tokens.get(tokens.size() - 1))) {
            big = Evaluator.group(tokens.get(tokens.size() - 1));
        } else {
            big = resultRaw != null ? Evaluator.group(resultRaw) : getString(R.string.result_zero);
        }

        String tape;
        if (error) {
            tape = errorReason;
        } else if (evaluated) {
            tape = lastExpression;
        } else if (!tokens.isEmpty()) {
            tape = TextUtils.join(" ", tokens);
        } else {
            tape = getString(R.string.tape_empty);
        }

        resultView.setText(big);
        tapeView.setText(tape.toUpperCase(Locale.US));
    }

    /** 120ms alpha settle, only when a value is final. */
    private void fadeReadout() {
        resultView.animate().cancel();
        resultView.setAlpha(0.3f);
        resultView.animate().alpha(1f).setDuration(120).start();
    }

    private void restore(Bundle state) {
        if (state == null) {
            return;
        }
        ArrayList<String> saved = state.getStringArrayList(STATE_TOKENS);
        if (saved != null) {
            tokens.clear();
            tokens.addAll(saved);
        }
        operand = state.getString(STATE_OPERAND, "");
        resultRaw = state.getString(STATE_RESULT, null);
        lastExpression = state.getString(STATE_EXPRESSION, "");
        evaluated = state.getBoolean(STATE_EVALUATED, false);
        error = state.getBoolean(STATE_ERROR, false);
        errorReason = state.getString(STATE_REASON, "");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_TOKENS, new ArrayList<>(tokens));
        outState.putString(STATE_OPERAND, operand);
        outState.putString(STATE_RESULT, resultRaw);
        outState.putString(STATE_EXPRESSION, lastExpression);
        outState.putBoolean(STATE_EVALUATED, evaluated);
        outState.putBoolean(STATE_ERROR, error);
        outState.putString(STATE_REASON, errorReason);
    }
}
