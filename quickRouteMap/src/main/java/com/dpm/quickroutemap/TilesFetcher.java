package com.dpm.quickroutemap;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MyMath;
import org.osmdroid.views.MapView;

import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import com.dpm.framework.Event;
import com.dpm.framework.EventArgs;
import com.dpm.framework.FileDownloader;
import com.dpm.framework.FileHelper;
import com.dpm.quickroutemap.navigation.Route;

/**
 * Descarga las losetas para almacenarlas en archivos locales.
 * 
 * @author David
 *
 */
public final class TilesFetcher{
	
	private final static String LOG_TAG = TilesFetcher.class.getSimpleName();
	private final static long STORED_TILES_TIMEOUT = 2592000000L; //30 días en milisegundos
	private final static String OSM_TILES_CACHE_ROOTPATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/tiles";
	private final static String QRM_TILES_CACHE_ROOTPATH = //QRM significa Quick Route Maps
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuickRouteMaps/tiles";

	/**
	 * Cuando avanza la descarga de mapas.
	 * Los avances están referidos en porcentajes del total, por lo que el trabajo estará finalizado
	 * cuando el avance total sea del 100%.
	 */
	public final Event<EventArgs> FetchFinished = new Event<EventArgs>();
	
	private final MapView _mapView;
	
	
	public TilesFetcher(MapView mapView){
		_mapView = mapView;
	}

    /**
     * Precarga las losetas del mapa dentro de un área.
     * @param route
     * 		El área que se descargará se ajustará a la ruta.
     * @param zoomLevel 
     * 		Nivel de zoom para el que se carga.
     * @param hasDataNetwork
     * 		Indica si el dispositivo tiene conexión de red de datos
     */
    public void fetchTiles(final Route route, final int zoomLevel, final boolean hasDataNetwork){
    	ExecutorService executor = Executors.newSingleThreadExecutor();
   		executor.submit(new Runnable() {
			public void run() {
				HashMap<String, Integer[]> tilesMap = new HashMap<String, Integer[]>();
				for(IGeoPoint waypoint: route.getWayPoints()){
					Point pixelCoord =
							TileSystem.LatLongToPixelXY(waypoint.getLatitudeE6() / 1E6, waypoint.getLongitudeE6() / 1E6, zoomLevel, null);
			    	Point tileCoord = TileSystem.PixelXYToTileXY(pixelCoord.x, pixelCoord.y, null);
			    	int mapTileUpperBound = 1 << zoomLevel;
			    	
			    	// Construct a MapTile to request from the tile provider.
			        int tileY = MyMath.mod(tileCoord.y, mapTileUpperBound);
			        int tileX = MyMath.mod(tileCoord.x, mapTileUpperBound);
			        String key = String.format(Locale.US, "%1$d,%2$d", tileX, tileY);
			        if(!tilesMap.containsKey(key)){
			        	tilesMap.put(key, new Integer[]{tileX, tileY});
			        }
				}
		        
				for(Integer[] tileCoord: tilesMap.values()){
					fetchWaypointTile(tileCoord[0], tileCoord[1], zoomLevel, hasDataNetwork);
				}
				FetchFinished.rise(this, EventArgs.empty);
			}
		});
    	executor.shutdown();
    }
    
    private void fetchWaypointTile(int tileX, int tileY, int zoomLevel, boolean hasDataNetwork){
    	
        MapTile tile = new MapTile(zoomLevel, tileX, tileY);
        
        Log.v(LOG_TAG, String.format("Loseta (zoom=%1$d, %2$d, %3$d)", zoomLevel, tileX, tileY));
        
        OnlineTileSourceBase tileSource = (OnlineTileSourceBase)_mapView.getTileProvider().getTileSource();
        String tileUrl = tileSource.getTileURLString(tile);
        
        String osmFilePath = String.format(Locale.US, "%1$s/%2$s/%3$d/%4$d/%5$d.png.tile",
        		OSM_TILES_CACHE_ROOTPATH, tileSource.name(), zoomLevel, tileX, tileY);
        File osmFile = new File(osmFilePath);
        
    	String qrmFilePath = String.format(Locale.US, "%1$s/%2$s/%3$d/%4$d/%5$d.png.tile",
        		QRM_TILES_CACHE_ROOTPATH, tileSource.name(), zoomLevel, tileX, tileY);
    	File qrmFile = new File(qrmFilePath);
    	
    	//El archivo debe estar en la caché de QRM. Si no está, se descarga.
    	if(hasDataNetwork && 
    			(!qrmFile.exists() || 
    					((new Date().getTime() -  qrmFile.lastModified()) > STORED_TILES_TIMEOUT))){
			FileDownloader.download(tileUrl, qrmFilePath);
    	}
    	
    	//Si se ha obtenido el archivo, o ya estaba, se copia a la caché de OSM
    	if(qrmFile.exists()){
    		if(!osmFile.exists() || (qrmFile.lastModified() != osmFile.lastModified())){
		    	try {	    		
					FileHelper.copy(qrmFile, osmFile);
					Log.v(LOG_TAG, "Loseta copiada a la caché de OSM.");
				} catch (IOException e) {
					Log.e(LOG_TAG, String.format("No se ha podido copiar de '%1$s' a '%2$s'.", qrmFilePath, osmFilePath));
					e.printStackTrace();
				}   	
    		}else{
    			Log.v(LOG_TAG, "La loseta ya está en la caché de OSM");
    		}
    	}else{
    		Log.w(LOG_TAG, String.format("El archivo '%1$s' no está en la caché QRM.", qrmFilePath));
    	}
    	
    }
   
}
