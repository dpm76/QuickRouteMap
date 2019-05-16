package com.dpm.quickroutemap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.dpm.framework.EventArgs;
import com.dpm.framework.EventDispatcher;
import com.dpm.framework.Invoker;
import com.dpm.framework.ParametrizedRunnable;
import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.GuidancePointProximityManager;
import com.dpm.quickroutemap.navigation.IGuidanceProvider;
import com.dpm.quickroutemap.navigation.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public class QuickRouteMapActivity extends Activity implements IGuidanceProvider {
	
	private String LOG_TAG = QuickRouteMapActivity.class.getSimpleName();
	
	private final static String DEFAULT_ROUTE_FILEPATH = "/data/ruta.txt";
	
	private final static int DEFAULT_ZOOM = 12;
	private static final int PICKFILE_RESULT_CODE = 1;
	
	private final static String IS_ZOOM_KEY = "zoom";
	private final static String IS_CENTER_LON_KEY = "center_lon";
	private final static String IS_CENTER_LAT_KEY = "center_lat";
	//private final static String IS_ROUTE = "route";
	private static Route _currentRoute; //TODO La ruta se debe guardar en _instanceState para recuperarla en onResume()
	
	//Se ha tenido que añadir el estado de forma explícita porque no llama a onRestoreInstanceState()
	private final static Bundle _instanceState = new Bundle();
	
	private final HashMap<String, RouteOverlay> _routeOverlaysMap = new HashMap<String, RouteOverlay>();
	
	private MapView _mapView;
	private IMapController _mapController;
	private OverlayManager _mapOverlayManager;
	private TilesFetcher _tilesFetcher;	
	private ExtendedMyLocationNewOverlay _myLocationOverlay;
	
	private ConnectivityManager _connectivityManager;
	//Indica el estado de la conexión de datos de la última vez que se comprobó
	private boolean _hasDataNetwork; 
	
	private GuidancePointProximityManager _proximityManager;
	private TextToSpeech _tts;
	private Gson _routeSerializer;
	
	private Invoker _invoker = new Invoker();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        _connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		HttpClientFactory.userAgent = getPackageName();
        _mapView = (MapView) findViewById(R.id.mapview);
        _tilesFetcher = new TilesFetcher(_mapView);
        _tilesFetcher.FetchFinished.add(new EventDispatcher<EventArgs>() {
			
			public void dispatch(Object arg0, EventArgs arg1) {
				runOnUiThread(new Runnable() {
					
					public void run() {
						onFetchFinished();						
					}
				});					
			}		
		});
        
        _mapView.setBuiltInZoomControls(true);
        if(this.getApplication().getPackageManager()
        		.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)){	        
	        _mapView.setMultiTouchControls(true);
        }else{        	
        	_mapView.setMultiTouchControls(false);
        }        
        
        _mapController = _mapView.getController();
        _mapOverlayManager = _mapView.getOverlayManager();
        
        _myLocationOverlay = new ExtendedMyLocationNewOverlay(this, _mapView);
        _mapOverlayManager.add(_myLocationOverlay);
        
        _mapController.setZoom(DEFAULT_ZOOM);        
        
        //Iniciar TTS
        _tts = new TextToSpeech(getApplicationContext(), 
        		new TextToSpeech.OnInitListener(){
        	
		        	@Override
		        	public void onInit(int status){
		        		if(status != TextToSpeech.ERROR){
		        			_tts.setLanguage(new Locale("es", "ES"));
		        		}		        		
		        	}		        	
        		});
        
        _proximityManager = new GuidancePointProximityManager(this, _tts, this);
        
        //Iniciar serializador
        _routeSerializer = new GsonBuilder()
        		.registerTypeAdapter(IGeoPoint.class, new GeoPointSerializer())
        		.create();
        
        saveState();
    }
    
    private void saveState(){
		Log.d(LOG_TAG, "saveState()");
		
		_instanceState.putInt(IS_ZOOM_KEY, _mapView.getZoomLevel());
		IGeoPoint center = _mapView.getMapCenter();
		int lon = center.getLongitudeE6();
		int lat = center.getLatitudeE6();
		if(lon != 0 && lat != 0){
			_instanceState.putInt(IS_CENTER_LON_KEY, center.getLongitudeE6());
			_instanceState.putInt(IS_CENTER_LAT_KEY, center.getLatitudeE6());
		}		
	}
	
	private void restoreState(){
		Log.d(LOG_TAG,"restoreState()");
        
        if(_instanceState.containsKey(IS_ZOOM_KEY)){
        	_mapController.setZoom(_instanceState.getInt(IS_ZOOM_KEY, DEFAULT_ZOOM));
        }
        
        if(_instanceState.containsKey(IS_CENTER_LAT_KEY) &&
        		_instanceState.containsKey(IS_CENTER_LON_KEY)){
        	
	        _mapController.setCenter(new GeoPoint(
	        		_instanceState.getInt(IS_CENTER_LAT_KEY),
	        		_instanceState.getInt(IS_CENTER_LON_KEY)));
        }        
       
        showRoute();       
	}
    
    @Override
	protected void onResume(){
		super.onResume();
		Log.d(LOG_TAG, "onResume()");
		restoreState();
		_myLocationOverlay.enableMyLocation();
		//_myLocationOverlay.enableCompass();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		Log.d(LOG_TAG, "onPause()");		
		_myLocationOverlay.disableMyLocation();
		//_myLocationOverlay.disableCompass();
		saveState();
	}
	
	@Override
	protected void onDestroy(){
		_tts.stop();
		_tts.shutdown();
		_proximityManager.close();
		
		super.onDestroy();
	}
	
	private void clear(){
		
		_mapOverlayManager.removeAll(_routeOverlaysMap.values());		
		_routeOverlaysMap.clear();
	}
	
	private void loadRoute(BufferedReader reader){
		
		Log.d(LOG_TAG, "update()");
		clear();		
			
		//Añadir rutas
		Log.d(LOG_TAG, "Añadiendo ruta");
		
		try {
			_currentRoute = _routeSerializer.fromJson(reader, Route.class);		
			
			_proximityManager.setRouteGuidance(_currentRoute.getGuidancePoints());
			
			showRoute();
			
			NetworkInfo networkInfo = _connectivityManager.getActiveNetworkInfo();
			_hasDataNetwork = (networkInfo != null) && networkInfo.isConnected(); 
			if(_hasDataNetwork){
				Toast.makeText(this, "Descargando mapas...", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(this, "Modo sin conexión", Toast.LENGTH_SHORT).show();
			}
			_tilesFetcher.fetchTiles(_currentRoute, DEFAULT_ZOOM, _hasDataNetwork);
			
			IGeoPoint center = _currentRoute.getWayPoints().get(0);
			_mapController.setCenter(center);
		} catch (JsonIOException e) {
			Log.e(LOG_TAG, "No se ha añadido la ruta");
		}
	}
	
	private void showRoute(){
		
		if(_currentRoute != null){
		
			RouteOverlay routeOverlay = new RouteOverlay(this, _currentRoute, 0xa0ff6010, 4f);
			_routeOverlaysMap.put(_currentRoute.getKey(), routeOverlay);
			_mapOverlayManager.add(0, routeOverlay);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		boolean handled = true;
		
		Log.d(LOG_TAG, item.getTitle().toString());
		switch (item.getItemId()){
			case R.id.userCenterMenuItem:			
				centerAtUserLocation();
				break;
			case R.id.openRouteFileMenuItem:
				launchRouteFileBrowser();				
				break;
			case R.id.resetZoomMenuItem:
				_mapController.setZoom(DEFAULT_ZOOM);
				break;
			default:
				handled = super.onOptionsItemSelected(item);
				break;
		}
		return handled;
	}
	
	private void centerAtUserLocation(){

		GeoPoint userLocation = _myLocationOverlay.getMyLocation();
		
		if(userLocation != null){
			Log.d(LOG_TAG, String.format("Centrando en posici�n del usuario en %1$s", userLocation.toString()));
			_mapController.setCenter(userLocation);
		}else{
			Log.w(LOG_TAG, "Posición de usuario Null");
		}
	}
	
	private void onFetchFinished() {
		
		if(_hasDataNetwork){
			new AlertDialog.Builder(this)
				.setTitle("Descarga finalizada")
				.setMessage("Descarga de mapas finalizada")
				.setPositiveButton(android.R.string.ok, null)
				.show();
			//_tts.speak("Descarga de mapas finalizada.", TextToSpeech.QUEUE_ADD, null);
		}else{
			Toast.makeText(this, "Copiado de mapas finalizado", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void launchRouteFileBrowser(){
		Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
		fileintent.addCategory(Intent.CATEGORY_OPENABLE);
        fileintent.setType("text/*");
        try {
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            Log.w(LOG_TAG, String.format("No se ha podido lanzar el navegador de archivos. Se usa el archivo por defecto: \"%s\"", DEFAULT_ROUTE_FILEPATH));
            loadRoute(DEFAULT_ROUTE_FILEPATH);
        }
	}
	
	private void loadRoute(String path){
		
		try {
			FileInputStream fis = new FileInputStream(path);
			loadRoute(new BufferedReader(new InputStreamReader(fis)));
			fis.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK){
        	String filePath = data.getData().getPath();
        	_invoker.invoke(
    			new ParametrizedRunnable(new Object[]{filePath}){
	        		public void run() {	        			
	        			String filePath = (String)_params[0];
	        			loadRoute(filePath);
//	        			try {        		
//							FileInputStream inputStream = new FileInputStream(filePath);
//							loadRoute(inputStream);
//							inputStream.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
		        	}
    			}
        	);
        }
	}

	@Override
	public GuidancePoint[] getCurrentRouteGuidance() {
		
		return _currentRoute != null ? _currentRoute.getGuidancePoints() : null;
	}
}