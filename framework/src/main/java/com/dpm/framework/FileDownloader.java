package com.dpm.framework;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	 */
    public static void download(final String sourceUrl, final String targetFilePath) {  //this is the downloader method

        Log.d(LOG_TAG, String.format("Comienzo de descarga desde '%1$s' a '%2$s'", sourceUrl, targetFilePath));
    	long startTime = System.currentTimeMillis();
        
    	try {
    		final URL url = new URL(sourceUrl);            
 
           
            BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
 
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
    
    public static Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
       } catch (IOException e) {
           Log.e(LOG_TAG, String.format("No se ha podido obtener \"%1$s\": %2$s", url, e.getMessage()));
       }
       return bm;
    } 

}
