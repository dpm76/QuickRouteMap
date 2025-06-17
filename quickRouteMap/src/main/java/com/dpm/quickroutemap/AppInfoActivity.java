package com.dpm.quickroutemap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AppInfoActivity extends Activity {

    private static final String LOG_TAG = AppInfoActivity.class.getSimpleName();

    public static final String NO_SHOW_ON_STARTUP_PREFERENCE = "no_show_on_startup";

    private CheckBox _noShowOnStartUpCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_info);

        TextView infoText = findViewById(R.id.info_text);
        Button closeButton = findViewById(R.id.close_battery_info_button);
        _noShowOnStartUpCheckBox = findViewById(R.id.no_show_on_startup);
        _noShowOnStartUpCheckBox.setOnClickListener(view -> onNoShowOnStartupCheckBoxClicked());
        _noShowOnStartUpCheckBox.setChecked(
                getSharedPreferences(AppInfoActivity.class.getSimpleName(), MODE_PRIVATE).getBoolean(NO_SHOW_ON_STARTUP_PREFERENCE, false));

        infoText.setText(
                "Para asegurar que esta aplicación funcione correctamente en segundo plano:\n\n" +
                        "1. Ve a Ajustes > Aplicaciones > QuickRouteMap > Batería.\n" +
                        "2. Desactiva la optimización de batería.\n" +
                        "3. Desactiva el modo de ahorro de energía del móvil para que funcione con la pantalla apagada.\n" +
                        "En dispositivos Samsung:\n" +
                        "   - Ajustes > Cuidado del dispositivo > Batería > Aplicaciones que nunca se suspenden.\n" +
                        "   - Añade esta aplicación a la lista."
        );

        closeButton.setOnClickListener(view -> finish());
    }

    private void onNoShowOnStartupCheckBoxClicked(){
        Log.d(LOG_TAG, String.format("noShowOnStartupCheckBox clicked: %s",
                _noShowOnStartUpCheckBox.isChecked()));

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putBoolean(NO_SHOW_ON_STARTUP_PREFERENCE, _noShowOnStartUpCheckBox.isChecked())
                .apply();
    }
}
