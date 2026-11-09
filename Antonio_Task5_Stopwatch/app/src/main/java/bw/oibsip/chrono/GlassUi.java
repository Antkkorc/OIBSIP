package bw.oibsip.chrono;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Shared glass theme behaviour: system bar insets, press motion and dialog styling.
 * Every screen calls install() before touching its views.
 */
public final class GlassUi {

    private static final float PRESS_SCALE = 0.97f;

    private GlassUi() {
    }

    public static void install(AppCompatActivity activity, int layoutRes) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        activity.setContentView(layoutRes);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        View root = activity.findViewById(R.id.root);
        if (root != null) {
            applyInsets(root);
        }
    }

    /** Keeps content clear of the status bar, navigation bar and any display cutout. */
    private static void applyInsets(final View root) {
        final int startPadLeft = root.getPaddingLeft();
        final int startPadTop = root.getPaddingTop();
        final int startPadRight = root.getPaddingRight();
        final int startPadBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(
                    startPadLeft + bars.left,
                    startPadTop + bars.top,
                    startPadRight + bars.right,
                    startPadBottom + bars.bottom);
            return insets;
        });
    }

    /** Press down to 0.97, release with a spring. Does not consume the touch, so clicks still fire. */
    public static void press(final View view) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().cancel();
                        v.animate().scaleX(PRESS_SCALE).scaleY(PRESS_SCALE)
                                .setDuration(90)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().cancel();
                        v.animate().scaleX(1f).scaleY(1f)
                                .setDuration(220)
                                .setInterpolator(new OvershootInterpolator(1.6f))
                                .start();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public static void pressAll(View... views) {
        for (View view : views) {
            press(view);
        }
    }

    /** Entrance for content blocks. Stagger the delay to give the screen an order. */
    public static void enter(View view, long delayMs) {
        if (view == null) {
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(10f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Short fade used when a value changes, so numbers never swap instantly. */
    public static void refresh(View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.setAlpha(0.35f);
        view.animate().alpha(1f).setDuration(180).start();
    }

    /** Rounded glass dialog anchored to the bottom of the screen. */
    public static Dialog dialog(AppCompatActivity activity, int layoutRes) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutRes);

        // A little air around the sheet so its rounded corners are visible.
        View frame = dialog.findViewById(android.R.id.content);
        if (frame != null) {
            int air = Math.round(12f * activity.getResources().getDisplayMetrics().density);
            frame.setPadding(air, 0, air, air);
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.55f;
            window.setAttributes(params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setBackgroundBlurRadius(48);
            }
        }
        return dialog;
    }
}
