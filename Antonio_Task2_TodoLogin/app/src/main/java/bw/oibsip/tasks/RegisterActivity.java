package bw.oibsip.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Creates an account, signs it in, and drops the user straight onto the task list. */
public class RegisterActivity extends AppCompatActivity {

    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private UserStore users;
    private SessionStore session;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private TextView errorText;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_register);

        users = new UserStore(this);
        session = new SessionStore(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        errorText = findViewById(R.id.errorText);
        createButton = findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        GlassUi.press(createButton);

        TextWatcher clearOnType = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideError();
            }
        };
        nameInput.addTextChangedListener(clearOnType);
        emailInput.addTextChangedListener(clearOnType);
        passwordInput.addTextChangedListener(clearOnType);
        confirmInput.addTextChangedListener(clearOnType);

        findViewById(R.id.signInLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GlassUi.enter(findViewById(R.id.registerCard), 60L);
    }

    private void submit() {
        final String name = nameInput.getText().toString().trim();
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();

        if (name.isEmpty()) {
            showError(getString(R.string.err_name));
            nameInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.err_email));
            emailInput.requestFocus();
            return;
        }
        if (password.length() < 8) {
            showError(getString(R.string.err_password));
            passwordInput.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            showError(getString(R.string.err_confirm));
            confirmInput.requestFocus();
            return;
        }

        setBusy(true);
        worker.execute(new Runnable() {
            @Override
            public void run() {
                final long id = users.register(name, email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setBusy(false);
                        if (id <= 0) {
                            showError(getString(R.string.err_taken));
                            return;
                        }
                        session.signIn(id);
                        startActivity(new Intent(RegisterActivity.this, TasksActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        createButton.setEnabled(!busy);
        createButton.setAlpha(busy ? 0.6f : 1f);
        createButton.setText(busy ? getString(R.string.creating) : getString(R.string.create_account));
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        GlassUi.refresh(errorText);
    }

    private void hideError() {
        if (errorText.getVisibility() == View.VISIBLE) {
            errorText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }
}
