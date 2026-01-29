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
        void onFileOpened(BufferedReader reader, Uri uri);

        void onError();
    }

    private static final String LOG_TAG = QuickRouteMapActivity.class.getSimpleName();

    public static final int PICK_FILE_REQUEST_CODE = 1101;
    public static final int CREATE_FILE_REQUEST_CODE = 1102;

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

    public void createFile(String suggestedName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        _activity.startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    public void handleFileResult(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            _callback.onError();
            return;
        }

        try (InputStream inputStream = _activity.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            _callback.onFileOpened(reader, uri);
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("I can not open file %1$s", uri.getPath()), e);
            _callback.onError();
        }
    }

    public boolean saveToFile(Uri uri, String content) {
        try (android.os.ParcelFileDescriptor pfd = _activity.getContentResolver().openFileDescriptor(uri, "rwt");
             java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(pfd.getFileDescriptor())) {
            fileOutputStream.write(content.getBytes());
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error saving file", e);
            return false;
        }
    }
}
