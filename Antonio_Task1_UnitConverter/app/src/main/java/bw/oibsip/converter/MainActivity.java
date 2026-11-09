package bw.oibsip.converter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Unit converter screen. Four glass chips choose the category, two spinners choose the
 * units, and the result lands in a card at the bottom of the content.
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_CATEGORY = "category";
    private static final String STATE_FROM = "from";
    private static final String STATE_TO = "to";
    private static final String STATE_INPUT = "input";
    private static final String STATE_RESULT = "result";
    private static final String STATE_SENTENCE = "sentence";
    private static final double MAX_INPUT = 1e15d;

    private EditText inputValue;
    private Spinner fromSpinner;
    private Spinner toSpinner;
    private TextView resultValue;
    private TextView resultSentence;
    private TextView categoryNote;
    private TextView errorText;
    private TextView[] chips;
    private View header;
    private View chipRow;
    private View cardInput;
    private View cardResult;
    private View dock;

    private ConverterEngine.Category category = ConverterEngine.Category.LENGTH;
    private boolean hasResult;
    private boolean restoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_main);

        inputValue = findViewById(R.id.inputValue);
        fromSpinner = findViewById(R.id.fromUnit);
        toSpinner = findViewById(R.id.toUnit);
        resultValue = findViewById(R.id.resultValue);
        resultSentence = findViewById(R.id.resultSentence);
        categoryNote = findViewById(R.id.categoryNote);
        errorText = findViewById(R.id.errorText);
        header = findViewById(R.id.header);
        chipRow = findViewById(R.id.chipRow);
        cardInput = findViewById(R.id.cardInput);
        cardResult = findViewById(R.id.cardResult);
        dock = findViewById(R.id.dock);

        chips = new TextView[]{
                findViewById(R.id.chipLength),
                findViewById(R.id.chipWeight),
                findViewById(R.id.chipVolume),
                findViewById(R.id.chipTemp)
        };

        for (int i = 0; i < chips.length; i++) {
            final ConverterEngine.Category target = ConverterEngine.Category.values()[i];
            chips[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (target != category) {
                        selectCategory(target, true);
                    }
                }
            });
        }
        GlassUi.pressAll(chips);

        View convertButton = findViewById(R.id.convertButton);
        View swapButton = findViewById(R.id.swapButton);
        GlassUi.press(convertButton);
        GlassUi.press(swapButton);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                convert(true);
            }
        });

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapUnits();
            }
        });

        resultValue.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!hasResult) {
                    return false;
                }
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(
                            ClipData.newPlainText("Converted value", resultValue.getText()));
                    Toast.makeText(MainActivity.this, R.string.toast_copied, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        inputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideError();
                if (hasResult && !restoring) {
                    convert(false);
                }
            }
        });

        ConverterEngine.Category startCategory = ConverterEngine.Category.LENGTH;
        if (savedInstanceState != null) {
            startCategory = ConverterEngine.Category.values()[
                    savedInstanceState.getInt(STATE_CATEGORY, 0)];
        }
        selectCategory(startCategory, false);

        if (savedInstanceState != null) {
            restoring = true;
            inputValue.setText(savedInstanceState.getString(STATE_INPUT, ""));
            fromSpinner.setSelection(clamp(savedInstanceState.getInt(STATE_FROM, 0), fromSpinner.getCount()));
            toSpinner.setSelection(clamp(savedInstanceState.getInt(STATE_TO, 1), toSpinner.getCount()));
            String result = savedInstanceState.getString(STATE_RESULT, null);
            if (result != null) {
                resultValue.setText(result);
                resultSentence.setText(savedInstanceState.getString(STATE_SENTENCE, null));
                hasResult = true;
            }
            restoring = false;
        }

        GlassUi.enter(header, 0L);
        GlassUi.enter(chipRow, 60L);
        GlassUi.enter(cardInput, 120L);
        GlassUi.enter(cardResult, 180L);
        GlassUi.enter(dock, 240L);
    }

    private static int clamp(int index, int count) {
        return Math.max(0, Math.min(index, Math.max(0, count - 1)));
    }

    /** Rebuilds both spinners for a category and clears the result card. */
    private void selectCategory(ConverterEngine.Category next, boolean clearResult) {
        category = next;

        for (int i = 0; i < chips.length; i++) {
            chips[i].setSelected(i == next.ordinal());
        }
        categoryNote.setText(noteFor(next));

        if (clearResult) {
            hasResult = false;
            resultValue.animate().cancel();
            resultValue.setAlpha(1f);
            resultValue.setText(R.string.result_placeholder);
            resultSentence.setText(R.string.awaiting);
            hideError();
        }

        List<String> labels = ConverterEngine.menuLabels(next);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_unit_selected, labels);
        adapter.setDropDownViewResource(R.layout.item_unit_dropdown);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (hasResult && !restoring) {
                    convert(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
        fromSpinner.setOnItemSelectedListener(listener);
        toSpinner.setOnItemSelectedListener(listener);
        fromSpinner.setSelection(0);
        toSpinner.setSelection(labels.size() > 1 ? 1 : 0);
    }

    private String noteFor(ConverterEngine.Category category) {
        switch (category) {
            case WEIGHT:
                return getString(R.string.note_weight);
            case VOLUME:
                return getString(R.string.note_volume);
            case TEMPERATURE:
                return getString(R.string.note_temp);
            case LENGTH:
            default:
                return getString(R.string.note_length);
        }
    }

    private void swapUnits() {
        int from = fromSpinner.getSelectedItemPosition();
        int to = toSpinner.getSelectedItemPosition();
        if (from < 0 || to < 0 || from == to) {
            return;
        }
        restoring = true;
        fromSpinner.setSelection(to);
        toSpinner.setSelection(from);
        restoring = false;
        if (hasResult) {
            convert(false);
        }
    }

    /**
     * Converts the current input. When loud is true, problems are reported with a toast
     * and a line under the result card. Silent conversions happen while typing.
     */
    private void convert(boolean loud) {
        String raw = inputValue.getText().toString().trim();
        if (raw.isEmpty()) {
            if (loud) {
                fail(getString(R.string.toast_empty));
            }
            return;
        }

        double value;
        try {
            value = ConverterEngine.parseInput(raw);
        } catch (NumberFormatException e) {
            if (loud) {
                fail(getString(R.string.toast_nan));
            }
            return;
        }

        if (Math.abs(value) > MAX_INPUT) {
            if (loud) {
                fail(getString(R.string.toast_too_big));
            }
            return;
        }

        int fromPos = fromSpinner.getSelectedItemPosition();
        int toPos = toSpinner.getSelectedItemPosition();
        List<ConverterEngine.Unit> units = ConverterEngine.units(category);
        if (fromPos < 0 || toPos < 0 || fromPos >= units.size() || toPos >= units.size()) {
            return;
        }
        ConverterEngine.Unit from = units.get(fromPos);
        ConverterEngine.Unit to = units.get(toPos);

        if (ConverterEngine.belowAbsoluteZero(value, from)) {
            if (loud) {
                fail(getString(R.string.toast_absolute_zero));
            }
            return;
        }

        double converted = ConverterEngine.convert(value, from, to);
        String text = ConverterEngine.format(converted);
        String sentence = ConverterEngine.sentence(value, from, to, text);

        boolean changed = !text.contentEquals(resultValue.getText());
        resultValue.setText(text);
        resultSentence.setText(sentence);
        hasResult = true;
        hideError();

        if (changed) {
            GlassUi.refresh(resultValue);
        }
    }

    private void fail(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        GlassUi.refresh(errorText);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        inputValue.requestFocus();
    }

    private void hideError() {
        if (errorText.getVisibility() == View.VISIBLE) {
            errorText.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inputValue.getWindowToken(), 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CATEGORY, category.ordinal());
        outState.putInt(STATE_FROM, fromSpinner.getSelectedItemPosition());
        outState.putInt(STATE_TO, toSpinner.getSelectedItemPosition());
        outState.putString(STATE_INPUT, inputValue.getText().toString());
        if (hasResult) {
            outState.putString(STATE_RESULT, resultValue.getText().toString());
            outState.putString(STATE_SENTENCE, resultSentence.getText().toString());
        }
    }
}
