package com.dpm.quickroutemap;

import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import com.dpm.framework.Event;
import com.dpm.framework.EventArgs;
import com.dpm.framework.FileDownloader;
import com.dpm.framework.FileHelper;
import com.dpm.quickroutemap.navigation.Route;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MyMath;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import microsoft.mappoint.TileSystem;

/**
 * Descarga las losetas para almacenarlas en archivos locales.
 * 
 * @author David
 *
 */
public final class TilesFetcher{
	
	private final static String LOG_TAG = TilesFetcher.class.getSimpleName();
	private final static long STORED_TILES_TIMEOUT = 2592000000L; //30 días en milisegundos
	private final static String QRM_TILES_CACHE_RELATIVE_PATH = "/QuickRouteMaps/tiles";

	/**
	 * Cuando avanza la descarga de mapas.
	 * Los avances están referidos en porcentajes del total, por lo que el trabajo estará finalizado
	 * cuando el avance total sea del 100%.
	 */
	public final Event<EventArgs> FetchFinished = new Event<>();
	
	private final MapView _mapView;
	private final String  _userAgent;
	private final String _qrmTilesCacheRootPath;
	private final String _osmTilesCacheRootPath;

	/**
	 * Creates a tile fetcher instance
	 * @param mapView MapView instance
	 * @param userAgent Name of the agent to be sent to the server
	 * @param qrmTilesCacheRootPath The path where tiles are pre-cached
	 * @param osmTilesCacheRootPath The path where OSMDroid stores the tiles
	 */
	public TilesFetcher(MapView mapView, String userAgent, String qrmTilesCacheRootPath, String osmTilesCacheRootPath){
		_mapView = mapView;
		_userAgent = userAgent;
		_qrmTilesCacheRootPath = qrmTilesCacheRootPath;
		_osmTilesCacheRootPath = osmTilesCacheRootPath;
	}

    /**
     * Precarga las teselas del mapa dentro de un área.
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
				final HashMap<String, Integer[]> tilesMap = new HashMap<>();
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
    	
        Log.d(LOG_TAG, String.format("Tesela (zoom=%1$d, %2$d, %3$d)", zoomLevel, tileX, tileY));
        
        final OnlineTileSourceBase tileSource = (OnlineTileSourceBase)_mapView.getTileProvider().getTileSource();
        final String tileUrl = String.format("%s%s/%s/%s.png", tileSource.getBaseUrl(), zoomLevel, tileX, tileY);

		final String qrmFilePath = String.format(Locale.US, "%1$s/%2$s/%3$d/%4$d/%5$d.png.tile",
				_qrmTilesCacheRootPath, tileSource.name(), zoomLevel, tileX, tileY);
    	final File qrmFile = new File(qrmFilePath);
    	//El archivo debe estar en la caché de QRM. Si no está, se descarga.
    	if(hasDataNetwork && 
    			(!qrmFile.exists() || 
    					((new Date().getTime() -  qrmFile.lastModified()) > STORED_TILES_TIMEOUT))){
			FileDownloader.download(_userAgent, tileUrl, qrmFilePath);
    	}

    	//Si se ha obtenido el archivo, o ya estaba, se copia a la caché de OSM
    	if(qrmFile.exists()){
			Log.d(LOG_TAG, String.format("Path to tile in QRM cache is '%1$s'", qrmFile.getAbsolutePath()));
			final String osmFilePath = String.format(Locale.US, "%1$s/%2$s/%3$d/%4$d/%5$d.png.tile",
					_osmTilesCacheRootPath, tileSource.name(), zoomLevel, tileX, tileY);
			final File osmFile = new File(osmFilePath);
    		if(!osmFile.exists() || (qrmFile.lastModified() > osmFile.lastModified())){
		    	try {	    		
					FileHelper.copy(qrmFile, osmFile);
					Log.d(LOG_TAG, "Tesela copiada a la caché de OSM.");
				} catch (IOException e) {
					Log.e(LOG_TAG, String.format("No se ha podido copiar de '%1$s' a '%2$s'.", qrmFile.getAbsolutePath(), osmFilePath));
					e.printStackTrace();
				}   	
    		}else{
    			Log.d(LOG_TAG, "La tesela ya está en la caché de OSM");
    		}
    	}else{
    		Log.w(LOG_TAG, String.format("El archivo '%1$s' no está en la caché QRM.", qrmFilePath));
    	}
    }
}
