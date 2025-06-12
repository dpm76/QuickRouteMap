package com.dpm.quickroutemap.navigation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.dpm.quickroutemap.QuickRouteMapActivity;
import com.dpm.quickroutemap.R;

import java.util.Locale;

public class GuidancePointProximityService extends Service {

    private static final String CHANNEL_ID = "com.dpm.quickroutemap.location_channel";
    private static final String LOG_TAG = GuidancePointProximityService.class.getSimpleName();

    private TextToSpeech _tts;
    private GuidancePointProximityManager _proximityManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "Starting foreground notification.");
        createNotificationChannel();
        startForeground(1, buildNotification());

        //Iniciar TTS
        Log.d(LOG_TAG, "Initialising TTS");
        _tts = new TextToSpeech(getApplicationContext(),
                status -> {
                    if (status != TextToSpeech.ERROR) {
                        Log.d(LOG_TAG, "TTS started");
                        //TODO Obtener idioma de la configuración del dispositivo
                        _tts.setLanguage(new Locale("es", "ES"));
                    }
                });
        Log.d(LOG_TAG, "Initialising proximity manager");
        _proximityManager = new GuidancePointProximityManager(GuidanceManager.getInstance(), _tts, this);
        GuidanceManager.getInstance().setConsumer(_proximityManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _proximityManager.close();

        _tts.stop();
        _tts.shutdown();
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, QuickRouteMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.mainNotificationChannelTitle))
                .setContentText(getResources().getString(R.string.mainNotificationChannelText))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(LOG_TAG, "createNotificationChannel()");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getResources().getString(R.string.mainNotificationChannelName),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getResources().getString(R.string.mainNotificationChannelDescription));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            } else {
                Log.e(LOG_TAG, "NotificationManager is null");
            }
        }
    }
}