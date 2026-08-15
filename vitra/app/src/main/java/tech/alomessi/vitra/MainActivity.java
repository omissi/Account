package tech.alomessi.vitra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class MainActivity extends Activity {
    private VitraView vitraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        SharedPreferences prefs = getSharedPreferences("vitra", Context.MODE_PRIVATE);
        vitraView = new VitraView(this, prefs);
        setContentView(vitraView);
        if (!prefs.contains("arabic")) {
            vitraView.postDelayed(this::showLanguageChooser, 350);
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(7, 9, 15));
        window.setNavigationBarColor(Color.rgb(7, 9, 15));
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void showLanguageChooser() {
        new AlertDialog.Builder(this)
            .setTitle("اختر اللغة · Choose language")
            .setMessage("يمكن تغيير اللغة لاحقًا من الإعدادات.\nYou can change it later in Settings.")
            .setPositiveButton("العربية", (dialog, which) -> vitraView.setArabic(true))
            .setNegativeButton("English", (dialog, which) -> vitraView.setArabic(false))
            .setCancelable(false)
            .show();
    }

    @Override
    public void onBackPressed() {
        if (vitraView != null && vitraView.navigateBack()) return;
        super.onBackPressed();
    }
}
