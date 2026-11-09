package bw.oibsip.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sign in screen. Hashing 120,000 PBKDF2 rounds takes real time, so it runs on a worker
 * thread with the button held down until the answer comes back.
 */
public class LoginActivity extends AppCompatActivity {

    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private UserStore users;
    private SessionStore session;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_login);

        users = new UserStore(this);
        session = new SessionStore(this);

        // A saved session skips this screen entirely.
        if (session.hasSession() && users.byId(session.userId()) != null) {
            openTasks();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.errorText);
        signInButton = findViewById(R.id.signInButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        GlassUi.press(signInButton);

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    return true;
                }
                return false;
            }
        });

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
        emailInput.addTextChangedListener(clearOnType);
        passwordInput.addTextChangedListener(clearOnType);

        findViewById(R.id.registerLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.glass_in, R.anim.glass_hold);
            }
        });

        GlassUi.enter(findViewById(R.id.brand), 0L);
        GlassUi.enter(findViewById(R.id.loginCard), 90L);
        GlassUi.enter(findViewById(R.id.registerLink), 170L);
    }

    private void submit() {
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();

        if (email.isEmpty()) {
            showError(getString(R.string.err_email));
            return;
        }
        if (password.isEmpty()) {
            showError(getString(R.string.err_password));
            return;
        }

        setBusy(true);
        worker.execute(new Runnable() {
            @Override
            public void run() {
                final UserStore.Account account = users.login(email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setBusy(false);
                        if (account == null) {
                            passwordInput.setText("");
                            showError(getString(R.string.err_login));
                            GlassUi.refresh(errorText);
                            return;
                        }
                        session.signIn(account.id);
                        openTasks();
                    }
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        signInButton.setEnabled(!busy);
        signInButton.setAlpha(busy ? 0.6f : 1f);
        signInButton.setText(busy ? getString(R.string.signing_in) : getString(R.string.sign_in));
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

    private void openTasks() {
        startActivity(new Intent(this, TasksActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }
}
