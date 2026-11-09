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
 * Instrument 01 — Converter.
 *
 * Layout thesis: legend gutter on the left, ruled rows down the middle, one ink readout
 * anchored to the bottom edge, one oxide action. Nothing is centred and nothing floats.
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
    private TextView[] tabs;

    private ConverterEngine.Category category = ConverterEngine.Category.LENGTH;
    private boolean hasResult;
    private boolean restoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputValue = findViewById(R.id.inputValue);
        fromSpinner = findViewById(R.id.fromUnit);
        toSpinner = findViewById(R.id.toUnit);
        resultValue = findViewById(R.id.resultValue);
        resultSentence = findViewById(R.id.resultSentence);
        categoryNote = findViewById(R.id.categoryNote);
        tabs = new TextView[]{
                findViewById(R.id.tabLength),
                findViewById(R.id.tabWeight),
                findViewById(R.id.tabVolume),
                findViewById(R.id.tabTemp)
        };

        for (int i = 0; i < tabs.length; i++) {
            final ConverterEngine.Category target = ConverterEngine.Category.values()[i];
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (target != category) {
                        selectCategory(target, true);
                    }
                }
            });
        }

        findViewById(R.id.convertButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                convert(true);
            }
        });

        findViewById(R.id.swapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapUnits();
            }
        });

        // Long press copies the readout — the one thing people actually want from a converter.
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
                    toast(getString(R.string.toast_copied));
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
                // Once a result exists the readout stays truthful: it re-converts silently.
                if (hasResult && !restoring) {
                    convert(false);
                }
            }
        });

        ConverterEngine.Category startCategory = ConverterEngine.Category.LENGTH;
        if (savedInstanceState != null) {
            int ordinal = savedInstanceState.getInt(STATE_CATEGORY, 0);
            startCategory = ConverterEngine.Category.values()[ordinal];
        }
        selectCategory(startCategory, false);

        if (savedInstanceState != null) {
            restoring = true;
            inputValue.setText(savedInstanceState.getString(STATE_INPUT, ""));
            int from = savedInstanceState.getInt(STATE_FROM, 0);
            int to = savedInstanceState.getInt(STATE_TO, 1);
            fromSpinner.setSelection(clamp(from, fromSpinner.getCount()));
            toSpinner.setSelection(clamp(to, toSpinner.getCount()));
            String result = savedInstanceState.getString(STATE_RESULT, null);
            String sentence = savedInstanceState.getString(STATE_SENTENCE, null);
            if (result != null) {
                resultValue.setText(result);
                resultSentence.setText(sentence);
                hasResult = true;
            }
            restoring = false;
        }
    }

    private static int clamp(int index, int count) {
        return Math.max(0, Math.min(index, Math.max(0, count - 1)));
    }

    /** Rebuilds both spinners for a category, and clears the readout unless told not to. */
    private void selectCategory(ConverterEngine.Category next, boolean resetReadout) {
        category = next;
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setSelected(i == next.ordinal());
        }
        categoryNote.setText(noteFor(next));

        if (resetReadout) {
            hasResult = false;
            resultValue.animate().cancel();
            resultValue.setAlpha(1f);
            resultValue.setText(R.string.result_placeholder);
            resultSentence.setText(R.string.awaiting);
        }

        List<String> labels = ConverterEngine.menuLabels(next);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_selected, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);

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

    private String noteFor(ConverterEngine.Category c) {
        switch (c) {
            case WEIGHT:
                return getString(R.string.category_weight_note);
            case VOLUME:
                return getString(R.string.category_volume_note);
            case TEMPERATURE:
                return getString(R.string.category_temp_note);
            case LENGTH:
            default:
                return getString(R.string.category_length_note);
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
     * @param loud when true, validation problems are surfaced as Toasts. Silent re-converts
     *             happen while typing and never interrupt.
     */
    private void convert(boolean loud) {
        String raw = inputValue.getText().toString().trim();
        if (raw.isEmpty()) {
            if (loud) {
                toast(getString(R.string.toast_empty));
                inputValue.requestFocus();
            }
            return;
        }

        double value;
        try {
            value = ConverterEngine.parseInput(raw);
        } catch (NumberFormatException e) {
            if (loud) {
                toast(getString(R.string.toast_nan));
                inputValue.requestFocus();
            }
            return;
        }

        if (Math.abs(value) > MAX_INPUT) {
            if (loud) {
                toast(getString(R.string.toast_too_big));
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
                toast(getString(R.string.toast_absolute_zero));
            }
            return;
        }

        double converted = ConverterEngine.convert(value, from, to);
        String text = ConverterEngine.format(converted);

        String sentence = ConverterEngine.sentence(value, from, to, text)
                + "  ·  via " + ConverterEngine.baseName(category);

        boolean changed = !text.contentEquals(resultValue.getText());
        resultValue.setText(text);
        resultSentence.setText(sentence);
        hasResult = true;

        if (changed) {
            fadeReadout();
        }
    }

    /** 120ms alpha settle — motion only because state changed. */
    private void fadeReadout() {
        resultValue.animate().cancel();
        resultValue.setAlpha(0.25f);
        resultValue.animate().alpha(1f).setDuration(120).start();
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inputValue.getWindowToken(), 0);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
