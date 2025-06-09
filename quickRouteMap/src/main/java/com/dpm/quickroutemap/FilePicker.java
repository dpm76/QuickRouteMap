package com.dpm.quickroutemap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FilePicker {
    public interface IFilePickerCallback{
        void onFileOpened(BufferedReader reader);

        void onError();
    }

    private static final String LOG_TAG = QuickRouteMapActivity.class.getSimpleName();

    public static final int PICK_FILE_REQUEST_CODE = 1101;

    private final IFilePickerCallback _callback;
    private final Activity _activity;

    public FilePicker(@NonNull Activity activity, @NonNull IFilePickerCallback callback){
        _callback = callback;
        _activity = activity;
    }

    public void openFilePicker(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.e(LOG_TAG, String.format("Minimal version is %1$s. I can not open files.", Build.VERSION_CODES.TIRAMISU));
            _callback.onError();
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        _activity.startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    public void handleFileResult(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            _callback.onError();
            return;
        }

        try (InputStream inputStream = _activity.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            _callback.onFileOpened(reader);
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("I can not open file %1$s", uri.getPath()));
            _callback.onError();
        }
    }
}
