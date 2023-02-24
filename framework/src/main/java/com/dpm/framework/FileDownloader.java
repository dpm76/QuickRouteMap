package com.dpm.framework;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utilidad para descargar archivos
 * @author David
 *
 */
public class FileDownloader {
	
	private static final String LOG_TAG = FileDownloader.class.getSimpleName();
    private static final int BUFFER_SIZE = 8192;
    private static final int TIMEOUT = 10000; // milliseconds
 
	/**
	 * Descarga un archivo
     * @param userAgent name of the agent to be sent to the server
	 * @param sourceUrl Origen del archivo
	 * @param targetFilePath Ruta local del archivo
	 */
    public static void download(final String userAgent, final String sourceUrl, final String targetFilePath) {

        Log.d(LOG_TAG, String.format("Download started from '%1$s' to '%2$s'", sourceUrl, targetFilePath));
    	final long startTime = System.currentTimeMillis();
        
    	try {

            // Try to create directories and file before connecting to server,
            // in order to save network connection.
            File file = new File(targetFilePath);
            if (!file.exists()) {
                String directoryPath = targetFilePath.substring(0, targetFilePath.lastIndexOf(File.separator));
                File directory = new File(directoryPath);
                if(!directory.exists() && !directory.mkdirs()) {
                    Log.e(LOG_TAG, String.format(
                            "I can't download because the directory '%1$s' doesn't exist and can't be created.",
                            directory.getAbsolutePath()));
                    return;
                }
                file.createNewFile();
            }

            final HttpURLConnection connection = (HttpURLConnection) new URL(sourceUrl).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestMethod("GET");

            // Check to see if we got success
            final int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e(LOG_TAG, String.format("Problem downloading resource: '%s' HTTP response code: %d", sourceUrl, responseCode));
                return;
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            int byteRead = -1;
            final byte[] buffer = new byte[BUFFER_SIZE];
            //Read bytes to the Buffer until there is nothing more to read(-1).
            BufferedInputStream reader = new BufferedInputStream(connection.getInputStream());
            do {
                int pos = 0;
                while (pos < BUFFER_SIZE && (byteRead = reader.read()) != -1) {
                    buffer[pos++] = (byte) byteRead;
                }
                if (pos != 0) {
                    fileOutputStream.write(buffer, 0, pos);
                }
            } while (byteRead != -1);
            fileOutputStream.flush();
            fileOutputStream.close();

            Log.d(LOG_TAG, String.format("Download finished from '%1$s' in %2$d seconds", sourceUrl, (System.currentTimeMillis() - startTime) / 1000));
        } catch (FileNotFoundException ex) {
            Log.e(LOG_TAG, String.format("Cannot create file '%1$s'", targetFilePath));
        } catch (IOException e) {        	
        	Log.e(LOG_TAG, String.format("Download error on URL '%1$s' :", sourceUrl));
        	e.printStackTrace();
        }
    }
}
