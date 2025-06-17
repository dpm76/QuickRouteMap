package com.dpm.quickroutemap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.dpm.quickroutemap.navigation.GuidanceManager;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.GuidancePointProximityService;
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
import java.util.HashMap;
import java.util.Objects;

public final class QuickRouteMapActivity extends Activity implements IGuidanceProvider {

    private static final String LOG_TAG = QuickRouteMapActivity.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final int BACKGROUND_PERMISSION_REQUEST_CODE = 1002;

    private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 1003;

// Maybe necessay when routes are cached from the cloud
//    private static final String ROUTES_DIR_PATH = "/routes/";
//    private static final String FILE_EXTENSION_JSON = ".json";

    private static final double DEFAULT_ZOOM = 12d;
    private final int ROUTE_COLOR = 0xa0ff6010;
    private final float ROUTE_WIDTH = 12f;

    private static final float DEFAULT_MAP_CENTER_LONGITUDE = 0f;
    private static final float DEFAULT_MAP_CENTER_LATITUDE = 0f;

    private static final String INTERNAL_STATE_ZOOM_KEY = "zoom";
    private static final String INTERNAL_STATE_CENTER_LON_KEY = "center_lon";
    private static final String INTERNAL_STATE_CENTER_LAT_KEY = "center_lat";
    private static Route _currentRoute; //TODO La ruta se debe guardar en _instanceState para recuperarla en onResume()

    private final HashMap<String, RouteOverlay> _routeOverlaysMap = new HashMap<>();

    private MapView _mapView;
    private IMapController _mapController;
    private OverlayManager _mapOverlayManager;
    private TilesFetcher _tilesFetcher;
    private ExtendedMyLocationNewOverlay _myLocationOverlay;
    private GuidanceManager _guidanceManager;
    private ConnectivityManager _connectivityManager;
    //Indica el estado de la conexión de datos de la última vez que se comprobó
    private boolean _hasDataNetwork;

    private Gson _routeSerializer;
    private FilePicker _filePicker;

    private String getDataPath(){

        return getBaseContext().getFilesDir().getAbsolutePath();
    }

    private void saveMapState(double latitude, double longitude, double zoom){
        Log.d(LOG_TAG,
                String.format("Saving: lat %1$f; lon %2$f; zoom %3$f", latitude, longitude, zoom));
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putFloat(INTERNAL_STATE_CENTER_LAT_KEY, (float) latitude)
                .putFloat(INTERNAL_STATE_CENTER_LON_KEY, (float) longitude)
                .putFloat(INTERNAL_STATE_ZOOM_KEY, (float) zoom)
                .apply();
    }

    private void restoreMapState(){
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        float longitude = preferences.getFloat(INTERNAL_STATE_CENTER_LON_KEY, DEFAULT_MAP_CENTER_LONGITUDE);
        float latitude = preferences.getFloat(INTERNAL_STATE_CENTER_LAT_KEY, DEFAULT_MAP_CENTER_LATITUDE);
        float zoom = preferences.getFloat(INTERNAL_STATE_ZOOM_KEY, (float)DEFAULT_ZOOM);
        Log.d(LOG_TAG,
                String.format("Reading: lat %1$f; lon %2$f; zoom %3$f", latitude, longitude, zoom));
        _mapController.setZoom(zoom);
        _mapController.setCenter(new GeoPoint(latitude, longitude));
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

        _guidanceManager = GuidanceManager.getInstance();

        checkPermissions();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String packageName = getPackageName();
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d(LOG_TAG, "Opening battery settings");
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        }

        //Iniciar serializador
        _routeSerializer = new GsonBuilder()
                .registerTypeAdapter(IGeoPoint.class, new GeoPointSerializer())
                .create();

        _filePicker = new FilePicker(this, new FilePicker.IFilePickerCallback() {
            @Override
            public void onFileOpened(BufferedReader reader) {
                loadRoute(reader);
            }

            @Override
            public void onError() {
                Toast.makeText(QuickRouteMapActivity.this, "No he podido leer ningún archivo", Toast.LENGTH_LONG).show();
            }
        });

        if(!getSharedPreferences(AppInfoActivity.class.getSimpleName(), MODE_PRIVATE)
                .getBoolean(AppInfoActivity.NO_SHOW_ON_STARTUP_PREFERENCE, false)){
            showInfo();
        }
    }

    private void saveState() {
        Log.d(LOG_TAG, "saveState()");

        double zoom = _mapView.getZoomLevelDouble();
        IGeoPoint center = _mapView.getMapCenter();
        double longitude = center.getLongitude();
        double latitude = center.getLatitude();

        saveMapState(latitude, longitude, zoom);
    }

    private void restoreState() {
        Log.d(LOG_TAG, "restoreState()");
        restoreMapState();
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
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
        saveState();
    }

    @Override
    protected void onDestroy() {
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
            _guidanceManager.setCurrentRouteGuidance(_currentRoute.getGuidancePoints());

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
            saveMapState(center.getLatitude(), center.getLongitude(), _mapView.getZoomLevelDouble());
        } catch (JsonIOException e) {
            Log.e(LOG_TAG, "No se ha añadido la ruta");
        }
    }

// Maybe necessary when routes are cached from cloud
//    private void loadRoute(String path) {
//
//        try {
//            FileInputStream fis = new FileInputStream(path);
//            loadRoute(new BufferedReader(new InputStreamReader(fis)));
//            fis.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void showRoute() {

        if (_currentRoute != null) {

            RouteOverlay routeOverlay = new RouteOverlay(_currentRoute, ROUTE_COLOR, ROUTE_WIDTH);
            _routeOverlaysMap.put(_currentRoute.getKey(), routeOverlay);
            _mapOverlayManager.add(0, routeOverlay);
        }
    }

    private void showInfo() {
        startActivity(new Intent(this, AppInfoActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(LOG_TAG, String.format("Selected main menu item: %1$s",
                Objects.requireNonNull(item.getTitle())));

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
                requestPermissionsManually();
                break;
            case R.id.appInfoMenuItem:
                showInfo();
                break;
            case R.id.closeAppMenuItem:
                stopService(new Intent(this, GuidancePointProximityService.class));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        } else {
            Toast.makeText(this, "Copiado de mapas finalizado", Toast.LENGTH_SHORT).show();
        }
    }

//    Old behaviour. Possible example for reading cached routes from cloud (when implemented)
//
//    private void launchRouteFileBrowser() {
//
//        File routesDir = new File(getDataPath() + ROUTES_DIR_PATH);
//        ArrayList<String> routePathList = new ArrayList<>();
//        File [] routeFiles = routesDir.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION_JSON));
//        if (routeFiles != null && routeFiles.length != 0) {
//            for (File routeFile : Objects.requireNonNull(routeFiles)) {
//
//                routePathList.add(routeFile.getName());
//            }
//        }
//
//        if(routePathList.size() != 0) {
//            new AlertDialog.Builder(this)
//                    .setTitle(R.string.selectRoute)
//                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
//                    .setItems(routePathList.toArray(new String[routePathList.size()]), (dialog, which)
//                            -> onRouteSelected(Collections.unmodifiableList(routePathList), which))
//                    .create().show();
//        } else {
//            new AlertDialog.Builder(this)
//                    .setMessage(R.string.noRouteMessage)
//                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
//                    .create().show();
//        }
//    }

    private void launchRouteFileBrowser() {
        _filePicker.openFilePicker();
    }

// Maybe necessary again when routes are cached from the cloud.
//    private void onRouteSelected(List<String> routePathList,  int which){
//
//        String routeName = routePathList.get(which);
//        Log.d(LOG_TAG, String.format("Selected route: '%1$s'", routeName));
//        String routeFilePath = String.format("%1$s%2$s%3$s", getDataPath(), ROUTES_DIR_PATH, routeName);
//        if (!new File(routeFilePath).exists()) {
//            Log.w(LOG_TAG, String.format("File '%1$s' doesn't exist!", routeFilePath));
//            Toast.makeText(this, "Route file not found!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        loadRoute(routeFilePath);
//    }

    @Override
    public GuidancePoint[] getCurrentRouteGuidance() {

        return _currentRoute != null ? _currentRoute.getGuidancePoints() : null;
    }

    private void checkPermissions(){
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE,
                new IPermissionCheckActions() {
                    @Override
                    public void doOnSuccess() {
                        checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                BACKGROUND_PERMISSION_REQUEST_CODE,
                                new IPermissionCheckActions() {
                                    @Override
                                    public void doOnSuccess() {
                                        checkPermission(Manifest.permission.POST_NOTIFICATIONS,
                                                POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE,
                                                new IPermissionCheckActions() {
                                                    @Override
                                                    public void doOnSuccess() {
                                                        startGuidancePointProximityService();
                                                    }

                                                    @Override
                                                    public void doOnFail() {
                                                        Toast.makeText(QuickRouteMapActivity.this,
                                                                "I can not post notifications!",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void doOnFail() {
                                        Toast.makeText(QuickRouteMapActivity.this,
                                                "No background location permission!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void doOnFail() {
                        Toast.makeText(QuickRouteMapActivity.this,
                                "No location permission!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final HashMap<Integer, IPermissionCheckActions> _actionsMap = new HashMap<>();

    private void checkPermission(String permission, int requestCode, IPermissionCheckActions permissionCheckActions){
        Log.d(LOG_TAG, String.format("Checking %s permission", permission));
        if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if(!_actionsMap.containsKey(requestCode)) {
                _actionsMap.put(requestCode, permissionCheckActions);
            }
            Log.d(LOG_TAG, String.format("Asking for %s permission", permission));
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    requestCode
            );
        }else{
            Log.d(LOG_TAG, String.format("Permission %s was already granted", permission));
            permissionCheckActions.doOnSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.i(LOG_TAG, String.format("User granted %s permission", permissions[0]));
            Objects.requireNonNull(_actionsMap.get(requestCode)).doOnSuccess();
        }else{
            Log.w(LOG_TAG, String.format("User denied %s permission", permissions[0]));
            Objects.requireNonNull(_actionsMap.get(requestCode)).doOnFail();
        }
    }

    private void requestPermissionsManually() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void startGuidancePointProximityService() {
        Log.d(LOG_TAG, "Starting GuidancePointProximityService");
        Intent serviceIntent = new Intent(this, GuidancePointProximityService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilePicker.PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            _filePicker.handleFileResult(data);
        }
    }
}