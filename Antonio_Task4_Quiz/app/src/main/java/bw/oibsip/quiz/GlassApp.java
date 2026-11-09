package bw.oibsip.quiz;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Sets the app to dark mode by default. The light palette is defined in
 * res/values/colors.xml with the same token names; remove the line below to
 * follow the system setting instead.
 */
public class GlassApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
