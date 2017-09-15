package com.dpm.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/**
 * Ayudas para simplificar el manejo de ficheros
 * 
 * @author David
 *
 */
public class FileHelper {
	
	private static String LOG_TAG = FileHelper.class.getSimpleName();
	
	/**
	 * Guarda un archivo en modo texto.
	 * 
	 * @param filePath Ruta del archivo
	 * @param string Contenido del archivo.
	 */
	public static void writeTextFile(String filePath, String string) throws IOException{
		FileOutputStream contentFile = new FileOutputStream(createFile(filePath));
		contentFile.write(string.getBytes());
		contentFile.close();
	}
	
	/**
	 * Lee un archivo en modo texto.
	 * 
	 * @param filePath Ruta del archivo
	 * @return Cadena de texto leída
	 */
	public static String readTextFile(String filePath) throws IOException{
		
		String string = null;
		
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[(int)file.length()];
		fis.read(buffer);
		fis.close();
		string = new String(buffer);
		
		return string;
	}
	
	/**
	 * Guarda un archivo en modo binario
	 * 
	 * @param filePath Ruta del archivo
	 * @param buffer 
	 * @throws IOException
	 */
	synchronized public static void writeBinaryFile(String filePath, byte[] buffer) throws IOException{
		
         FileOutputStream fos = new FileOutputStream(createFile(filePath));
         fos.write(buffer);
         fos.close();
         Log.v(LOG_TAG, String.format("Guardado \"%1$s\"", filePath));
	}
	
	/**
	 * Crea un archivo con toda la ruta completa
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private static File createFile(String filePath) throws IOException{
		
		return createFile(new File(filePath));
	}
	
	/**
	 * Crea un archivo con toda la ruta completa
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static File createFile(File file) throws IOException{
		
		Log.d(LOG_TAG, String.format("CreateFile(\"%1$s\")", file.getAbsolutePath()));
				 
        if(!file.getParentFile().exists()){
        	file.getParentFile().mkdirs();
        }
        
        if(!file.exists()){
        	file.createNewFile();
        }
        
        return file;	
	}
	
	/**
	 * Copia el archivo de origen en el destino. Si existe el destino,
	 * será reemplazado por el origen.
	 * @param src origen
	 * @param dst destino
	 * @throws IOException
	 */
	public static void copy(File src, File dst) throws IOException {
		
		if(dst.exists()){
			dst.delete();
		}else{
			createFile(dst);
		}
		
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
}
