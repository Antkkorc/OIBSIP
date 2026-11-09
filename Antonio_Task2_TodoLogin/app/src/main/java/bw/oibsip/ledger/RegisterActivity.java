package bw.oibsip.ledger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Create an account. Validation is explicit so the error names the actual problem. */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD = 8;
    private static final int MIN_NAME = 2;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private UserStore users;
    private SessionStore session;

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;
    private View createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        users = new UserStore(this);
        session = new SessionStore(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.errorText);
        createButton = findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        findViewById(R.id.signInLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void submit() {
        final String name = nameInput.getText().toString().trim();
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();

        if (name.length() < MIN_NAME) {
            showError(getString(R.string.error_name));
            nameInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_email));
            emailInput.requestFocus();
            return;
        }
        if (password.length() < MIN_PASSWORD) {
            showError(getString(R.string.error_password));
            passwordInput.requestFocus();
            return;
        }

        errorText.setVisibility(View.GONE);
        createButton.setEnabled(false);

        worker.execute(new Runnable() {
            @Override
            public void run() {
                final long userId = users.register(name, email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        createButton.setEnabled(true);
                        if (userId == -1) {
                            showError(getString(R.string.error_email_taken));
                            emailInput.requestFocus();
                            return;
                        }
                        session.signIn(userId);
                        Intent intent = new Intent(RegisterActivity.this, TasksActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }
}
