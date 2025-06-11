package com.dpm.quickroutemap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AppInfoActivity extends Activity {

    private static final String LOG_TAG = AppInfoActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_info);

        TextView infoText = findViewById(R.id.info_text);
        Button closeButton = findViewById(R.id.close_battery_info_button);

        infoText.setText(
                "Para asegurar que esta aplicación funcione correctamente en segundo plano:\n\n" +
                        "1. Ve a Ajustes > Aplicaciones > QuickRouteMap > Batería.\n" +
                        "2. Desactiva la optimización de batería.\n" +
                        "3. En dispositivos Samsung:\n" +
                        "   - Ajustes > Cuidado del dispositivo > Batería > Aplicaciones que nunca se suspenden.\n" +
                        "   - Añade esta aplicación a la lista."
        );

        closeButton.setOnClickListener(v -> {
            finish();
        });
    }
}
