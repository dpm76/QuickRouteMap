package com.dpm.quickroutemap.navigation;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;

public final class GuidancePointProximityManager {

    private static final String CONTENT_PROX_ALERT = "com.dpm.quickroutemap.GUIDANCE-POINT_PROXIMITY_ALERT";
    private static final String LOG_TAG = GuidancePointProximityManager.class.getSimpleName();

    private final LocationManager _locationManager;
    private final ProximityReceiver _proximityReceiver;
    private final ArrayList<PendingIntent> _pIntentsCollection = new ArrayList<PendingIntent>();
    private final ContextWrapper _context;
    private boolean _gpsReady = false;
    private boolean _gpsFixed = false;
    private IGuidanceProvider _guidanceProvider;

//	private final EventDispatcher<EventArgs> _routeChangedDispatcher = new EventDispatcher<EventArgs>() {
//		
//		@Override
//		public void dispatch(Object o, EventArgs args) {
//			onRouteChanged();			
//		}
//	};

    public GuidancePointProximityManager(IGuidanceProvider guidanceProvider, TextToSpeech tts, ContextWrapper context) {
        _proximityReceiver = new ProximityReceiver(tts);
        _guidanceProvider = guidanceProvider;
        _context = context;
        _locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        //TODO 20170903 DPM - addGpsStatusListener() method was deprecated in API level 24.
        // use registerGnssStatusCallback(GnssStatus.Callback) instead.
        if (_context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "The application hasn't ACCESS_FINE_LOCATION permission!");
        }

        _locationManager.addGpsStatusListener(new Listener() {

            @Override
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        Log.d(LOG_TAG, "GPS started");
                        _gpsReady = true;
                        if (_gpsFixed) {
                            setRouteGuidance(_guidanceProvider.getCurrentRouteGuidance());
                        }
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        Log.d(LOG_TAG, "GPS first fix.");
                        _gpsFixed = true;
                        setRouteGuidance(_guidanceProvider.getCurrentRouteGuidance());
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        Log.d(LOG_TAG, "GPS stopped");
                        _gpsReady = false;
                        removeRouteGuidance();
                        break;
                    default:
                        break;
                }

            }
        });
        _context.registerReceiver(_proximityReceiver, new IntentFilter(CONTENT_PROX_ALERT));
        //registerEvents();
    }


//	private void registerEvents(){
//		//TODO registrar _routeChangedDispatcher en evento de ruta cambiada en el gestor de rutas
//		//_contentManager.bundleChanged.add(_bundleChangedDispatcher);
//	}
//	
//	private void unregisterEvents(){
//		//TODO desregistrar _routeChangedDispatcher en evento de ruta cambiada en el gestor de rutas
//		//_contentManager.bundleChanged.remove(_bundleChangedDispatcher);
//	}
//	
//	private void setCurrentRoute(){
//		//TODO Obtener ruta actual del gestor de rutas
//		setRoute(/*ruta actual*/);
//	}

    public void setRouteGuidance(GuidancePoint[] routeGuidance){

        if(hasAlerts()){
            removeRouteGuidance();
        }

        if(routeGuidance != null){
            Log.d(LOG_TAG, "Añadiendo guiado");
            for(GuidancePoint guidancePoint: routeGuidance){
                addGuidancePoint(guidancePoint);
            }
        }
    }

    private boolean hasAlerts(){
        return (_pIntentsCollection.size() > 0);
    }

    private void removeRouteGuidance(){
        Log.d(LOG_TAG, "Eliminando ruta");
        for(PendingIntent pIntent: _pIntentsCollection){
            _locationManager.removeProximityAlert(pIntent);
        }
        _pIntentsCollection.clear();
    }

    private void addGuidancePoint(GuidancePoint guidancePoint){

        Log.d(LOG_TAG,"Añadiendo " + guidancePoint.getKey());

        double latitude = guidancePoint.getLatitude();
        double longitude = guidancePoint.getLongitude();
        float radius = guidancePoint.getRadius();
        String key =  guidancePoint.getKey();
        String narrative = guidancePoint.getNarrative();

        Intent intent = new Intent(CONTENT_PROX_ALERT);
        intent.putExtra(ProximityReceiver.GUIDANCE_POINT_KEY, key);
        intent.putExtra(ProximityReceiver.GUIDANCE_POINT_NARRATIVE,
                //TODO 20170909 DPM - Localize string
                String.format("A %1$.0f metros: %2$s", radius, narrative));
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(_context.getApplicationContext(),
                        _pIntentsCollection.size(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Log.d(LOG_TAG, String.format("(%1$.6f,%2$.6f,%3$.6f)", latitude, longitude, radius));
        if (_context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "The application hasn't ACCESS_FINE_LOCATION permission!");
        }
        _locationManager.addProximityAlert(latitude, longitude, radius, -1, pendingIntent);
        _pIntentsCollection.add(pendingIntent);
    }

    public void close(){
        Log.d(LOG_TAG, "Cerrando");
        _context.unregisterReceiver(_proximityReceiver);
        removeRouteGuidance();
        //unregisterEvents();
    }

//	private void onRouteChanged(){
//		if (_gpsReady){
//			setCurrentRoute();		
//		}
//	}
}
