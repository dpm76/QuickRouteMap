package com.dpm.quickroutemap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.GuidancePointProximityManager;
import com.dpm.quickroutemap.navigation.IGuidanceProvider;
import com.dpm.quickroutemap.navigation.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class QuickRouteMapActivity extends Activity implements IGuidanceProvider {

    private final String LOG_TAG = QuickRouteMapActivity.class.getSimpleName();

    private final static int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1002;

    private final static String ROUTES_DIR_PATH = "/routes/";
    private final static String FILE_EXTENSION_JSON = ".json";

    private final static double DEFAULT_ZOOM = 12d;
    private final int ROUTE_COLOR = 0xa0ff6010;
    private final float ROUTE_WIDTH = 12f;

    private final static String INTERNAL_STATE_ZOOM_KEY = "zoom";
    private final static String INTERNAL_STATE_CENTER_LON_KEY = "center_lon";
    private final static String INTERNAL_STATE_CENTER_LAT_KEY = "center_lat";
    private static Route _currentRoute; //TODO La ruta se debe guardar en _instanceState para recuperarla en onResume()

    //Se ha tenido que añadir el estado de forma explícita porque no llama a onRestoreInstanceState()
    private final static Bundle _instanceState = new Bundle();

    private final HashMap<String, RouteOverlay> _routeOverlaysMap = new HashMap<>();

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

    private String getDataPath(){

        return getBaseContext().getFilesDir().getAbsolutePath();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider mapConfig = Configuration.getInstance();
        mapConfig.setUserAgentValue(getPackageName());
        File osmBasePath = new File(getDataPath() + "/osmdroid");
        mapConfig.setOsmdroidBasePath(osmBasePath);
        Log.d(LOG_TAG, String.format("OSM-base-path: '%1$s'", mapConfig.getOsmdroidBasePath().getAbsolutePath()));

        setContentView(R.layout.main);

        _connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        _mapView = findViewById(R.id.mapview);
        // Force to use the file cache instead the default database cache
        _mapView.setTileProvider(new MapTileProviderBasic(this,
                new XYTileSource("Mapnik", 1, 18, 256,
                        ".png", new String[]{
                        "https://a.tile.openstreetmap.org/",
                        "https://b.tile.openstreetmap.org/",
                        "https://c.tile.openstreetmap.org/"
                }),
                new TileWriter()));
        //_mapView.setUseDataConnection(false); // Uncomment to debug without connection
        _tilesFetcher = new TilesFetcher(_mapView, getPackageName(),
                getDataPath() + "/tiles",
                mapConfig.getOsmdroidTileCache().getAbsolutePath());
        _tilesFetcher.FetchFinished.add((arg0, arg1) -> runOnUiThread(this::onFetchFinished));

        _mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        _mapView.setMultiTouchControls(this.getApplication().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH));

        _mapController = _mapView.getController();
        _mapOverlayManager = _mapView.getOverlayManager();

        _myLocationOverlay = new ExtendedMyLocationNewOverlay(this, _mapView);
        _mapOverlayManager.add(_myLocationOverlay);

        _mapController.setZoom(DEFAULT_ZOOM);

        //Iniciar TTS
        _tts = new TextToSpeech(getApplicationContext(),
                status -> {
                    if (status != TextToSpeech.ERROR) {
                        _tts.setLanguage(new Locale("es", "ES"));
                    }
                });

        _proximityManager = new GuidancePointProximityManager(this, _tts, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }else{
            _proximityManager.init();
        }

        //Iniciar serializador
        _routeSerializer = new GsonBuilder()
                .registerTypeAdapter(IGeoPoint.class, new GeoPointSerializer())
                .create();

        saveState();
    }

    private void saveState() {
        Log.d(LOG_TAG, "saveState()");

        _instanceState.putDouble(INTERNAL_STATE_ZOOM_KEY, _mapView.getZoomLevelDouble());
        IGeoPoint center = _mapView.getMapCenter();
        double longitude = center.getLongitude();
        double latitude = center.getLatitude();
        if (longitude != 0 && latitude != 0) {
            _instanceState.putDouble(INTERNAL_STATE_CENTER_LON_KEY, longitude);
            _instanceState.putDouble(INTERNAL_STATE_CENTER_LAT_KEY, latitude);
        }
    }

    private void restoreState() {
        Log.d(LOG_TAG, "restoreState()");

        if (_instanceState.containsKey(INTERNAL_STATE_ZOOM_KEY)) {
            _mapController.setZoom(_instanceState.getDouble(INTERNAL_STATE_ZOOM_KEY, DEFAULT_ZOOM));
        }

        if (_instanceState.containsKey(INTERNAL_STATE_CENTER_LAT_KEY) &&
                _instanceState.containsKey(INTERNAL_STATE_CENTER_LON_KEY)) {

            _mapController.setCenter(new GeoPoint(
                    _instanceState.getDouble(INTERNAL_STATE_CENTER_LAT_KEY),
                    _instanceState.getDouble(INTERNAL_STATE_CENTER_LON_KEY)));
        }

        showRoute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        restoreState();
        _myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
        _myLocationOverlay.disableMyLocation();
        saveState();
    }

    @Override
    protected void onDestroy() {
        _tts.stop();
        _tts.shutdown();
        _proximityManager.close();

        super.onDestroy();
    }

    private void clear() {

        _mapOverlayManager.removeAll(_routeOverlaysMap.values());
        _routeOverlaysMap.clear();
    }

    private void loadRoute(BufferedReader reader) {

        Log.d(LOG_TAG, "loadRoute()");
        clear();

        //Añadir rutas
        Log.d(LOG_TAG, "Añadiendo ruta");

        try {
            _currentRoute = _routeSerializer.fromJson(reader, Route.class);

            _proximityManager.setRouteGuidance(_currentRoute.getGuidancePoints());

            showRoute();

            NetworkInfo networkInfo = _connectivityManager.getActiveNetworkInfo();
            _hasDataNetwork = (networkInfo != null) && networkInfo.isConnected();
            if (_hasDataNetwork) {
                Toast.makeText(this, "Descargando mapas...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Modo sin conexión", Toast.LENGTH_SHORT).show();
            }
            _tilesFetcher.fetchTiles(_currentRoute, (int) DEFAULT_ZOOM, _hasDataNetwork);

            IGeoPoint center = _currentRoute.getWayPoints().get(0);
            _mapController.setCenter(center);
        } catch (JsonIOException e) {
            Log.e(LOG_TAG, "No se ha añadido la ruta");
        }
    }

    private void loadRoute(String path) {

        try {
            FileInputStream fis = new FileInputStream(path);
            loadRoute(new BufferedReader(new InputStreamReader(fis)));
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRoute() {

        if (_currentRoute != null) {

            RouteOverlay routeOverlay = new RouteOverlay(_currentRoute, ROUTE_COLOR, ROUTE_WIDTH);
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

        Log.d(LOG_TAG, Objects.requireNonNull(item.getTitle()).toString());
        switch (item.getItemId()) {
            case R.id.userCenterMenuItem:
                centerAtUserLocation();
                break;
            case R.id.openRouteFileMenuItem:
                launchRouteFileBrowser();
                break;
            case R.id.resetZoomMenuItem:
                _mapController.setZoom(DEFAULT_ZOOM);
                break;
            case R.id.locationPermissionMenuItem:
                requestBackgroundLocationPermission();
                break;
            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }
        return handled;
    }

    private void centerAtUserLocation() {

        GeoPoint userLocation = _myLocationOverlay.getMyLocation();

        if (userLocation != null) {
            Log.d(LOG_TAG, String.format("Centrando en posición del usuario en %1$s", userLocation));
            _mapController.setCenter(userLocation);
        } else {
            Log.w(LOG_TAG, "Posición de usuario Null");
        }
    }

    private void onFetchFinished() {

        if (_hasDataNetwork) {
            new AlertDialog.Builder(this)
                    .setTitle("Descarga finalizada")
                    .setMessage("Descarga de mapas finalizada")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            //_tts.speak("Descarga de mapas finalizada.", TextToSpeech.QUEUE_ADD, null);
        } else {
            Toast.makeText(this, "Copiado de mapas finalizado", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchRouteFileBrowser() {

        File routesDir = new File(getDataPath() + ROUTES_DIR_PATH);
        ArrayList<String> routePathList = new ArrayList<>();
        File [] routeFiles = routesDir.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION_JSON));
        if (routeFiles != null && routeFiles.length != 0) {
            for (File routeFile : Objects.requireNonNull(routeFiles)) {

                routePathList.add(routeFile.getName());
            }
        }

        if(routePathList.size() != 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.selectRoute)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setItems(routePathList.toArray(new String[routePathList.size()]), (dialog, which)
                            -> onRouteSelected(Collections.unmodifiableList(routePathList), which))
                    .create().show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.noRouteMessage)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    private void onRouteSelected(List<String> routePathList,  int which){

        String routeName = routePathList.get(which);
        Log.d(LOG_TAG, String.format("Selected route: '%1$s'", routeName));
        String routeFilePath = String.format("%1$s%2$s%3$s", getDataPath(), ROUTES_DIR_PATH, routeName);
        if (!new File(routeFilePath).exists()) {
            Log.w(LOG_TAG, String.format("File '%1$s' doesn't exist!", routeFilePath));
            Toast.makeText(this, "Route file not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        loadRoute(routeFilePath);
    }

    @Override
    public GuidancePoint[] getCurrentRouteGuidance() {

        return _currentRoute != null ? _currentRoute.getGuidancePoints() : null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                _proximityManager.init();
                requestBackgroundLocationPermission();
            } else {
                Log.i(LOG_TAG, "User rejected the location permission");
                Toast.makeText(this, "No location permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestBackgroundLocationPermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

}