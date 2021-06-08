package com.dpm.framework;

import java.io.BufferedInputStream;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.ByteArrayBuffer;
import android.util.Log;

/**
 * Utilidad para descargar archivos
 * @author David
 *
 */
public class FileDownloader {
	
	private static final String LOG_TAG = FileDownloader.class.getSimpleName();
 
	/**
	 * Descarga un archivo
	 * @param sourceUrl Origen del archivo
	 * @param targetFilePath Ruta local del archivo
     * @param client HTTP client object
	 */
    public static void download(final String sourceUrl, final String targetFilePath, final HttpClient client) {  //this is the downloader method

        Log.d(LOG_TAG, String.format("Comienzo de descarga desde '%1$s' a '%2$s'", sourceUrl, targetFilePath));
    	long startTime = System.currentTimeMillis();
        
    	try {

            final HttpUriRequest head = new HttpGet(sourceUrl);
            final HttpResponse response = client.execute(head);

            // Check to see if we got success
            final org.apache.http.StatusLine line = response.getStatusLine();
            if (line.getStatusCode() != 200) {
                Log.w(LOG_TAG, "Problem downloading resource: " + sourceUrl + " HTTP response: " + line);
                return;
            }

            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                Log.w(LOG_TAG, "No content downloading resource: " + sourceUrl);
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(entity.getContent());
 
            //Read bytes to the Buffer until there is nothing more to read(-1).
            ByteArrayBuffer baf = new ByteArrayBuffer(8192);
            int current = 0;
            while ((current = bis.read()) != -1) {
            	baf.append((byte) current);
            }
            FileHelper.writeBinaryFile(targetFilePath, baf.toByteArray());
 
            Log.d(LOG_TAG, String.format("Finalizada la descarga desde '%1$s' en %2$d segundos", sourceUrl, (System.currentTimeMillis() - startTime)/1000));
        } catch (IOException e) {        	
        	Log.e(LOG_TAG, String.format("Error al descargar '%1$s' :", sourceUrl));
        	e.printStackTrace();
        }
 
    }
}
