package bw.oibsip.ledger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sign in. PBKDF2 at 120,000 iterations takes real milliseconds, so the work happens on
 * a single background thread with the button disabled — the UI never freezes mid-tap.
 */
public class LoginActivity extends AppCompatActivity {

    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private UserStore users;
    private SessionStore session;

    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;
    private View signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        users = new UserStore(this);
        session = new SessionStore(this);

        // A stored session goes straight to the ledger; nothing is re-typed.
        if (session.hasSession() && users.byId(session.userId()) != null) {
            openLedger();
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

        findViewById(R.id.registerLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    return true;
                }
                return false;
            }
        });
    }

    private void submit() {
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.error_login_empty));
            return;
        }

        errorText.setVisibility(View.GONE);
        signInButton.setEnabled(false);

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
                        signInButton.setEnabled(true);
                        if (account == null) {
                            passwordInput.setText("");
                            showError(getString(R.string.error_login_failed));
                            passwordInput.requestFocus();
                            return;
                        }
                        session.signIn(account.id);
                        openLedger();
                    }
                });
            }
        });
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    /** Clears the back stack: after signing in, back should leave the app, not the form. */
    private void openLedger() {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }
}
