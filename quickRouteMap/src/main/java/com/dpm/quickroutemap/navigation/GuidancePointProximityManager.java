package com.dpm.quickroutemap.navigation;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GnssStatus.Callback;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public final class GuidancePointProximityManager {

    private static final String CONTENT_PROXIMITY_ALERT = "com.dpm.quickroutemap.GUIDANCE-POINT_PROXIMITY_ALERT";
    private static final String LOG_TAG = GuidancePointProximityManager.class.getSimpleName();

    private final LocationManager _locationManager;
    private final ProximityReceiver _proximityReceiver;
    private final ArrayList<PendingIntent> _pIntentsCollection = new ArrayList<PendingIntent>();
    private final ContextWrapper _context;
    private boolean _gpsReady = false;
    private boolean _gpsFixed = false;
    private final IGuidanceProvider _guidanceProvider;

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
        if (_context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "The application hasn't ACCESS_FINE_LOCATION permission!");
            Toast.makeText(context, "No location permission!", Toast.LENGTH_SHORT).show();
            return;
        }

        _locationManager.registerGnssStatusCallback(new Callback() {
            @Override
            public void onStarted() {

                Log.d(LOG_TAG, "GPS started");
                _gpsReady = true;
                if (_gpsFixed) {
                    setRouteGuidance(_guidanceProvider.getCurrentRouteGuidance());
                }
            }

            @Override
            public void onStopped() {
                Log.d(LOG_TAG, "GPS stopped");
                _gpsReady = false;
                removeRouteGuidance();
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                Log.d(LOG_TAG, String.format("GPS first fix after %1$d seconds.", ttffMillis / 1000));
                _gpsFixed = true;
                setRouteGuidance(_guidanceProvider.getCurrentRouteGuidance());
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
            }
        });
        _context.registerReceiver(_proximityReceiver, new IntentFilter(CONTENT_PROXIMITY_ALERT));
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

    public void setRouteGuidance(GuidancePoint[] routeGuidance) {

        if (hasAlerts()) {
            removeRouteGuidance();
        }

        if (routeGuidance != null) {
            Log.d(LOG_TAG, "Añadiendo guiado");
            for (GuidancePoint guidancePoint : routeGuidance) {
                addGuidancePoint(guidancePoint);
            }
        }
    }

    private boolean hasAlerts() {
        return (_pIntentsCollection.size() > 0);
    }

    private void removeRouteGuidance() {
        Log.d(LOG_TAG, "Eliminando ruta");
        for (PendingIntent pIntent : _pIntentsCollection) {
            _locationManager.removeProximityAlert(pIntent);
        }
        _pIntentsCollection.clear();
    }

    private void addGuidancePoint(GuidancePoint guidancePoint) {

        Log.d(LOG_TAG, "Añadiendo " + guidancePoint.getKey());

        double latitude = guidancePoint.getLatitude();
        double longitude = guidancePoint.getLongitude();
        float radius = guidancePoint.getRadius();
        String key = guidancePoint.getKey();
        String narrative = guidancePoint.getNarrative();

        Intent intent = new Intent(CONTENT_PROXIMITY_ALERT);
        intent.putExtra(ProximityReceiver.GUIDANCE_POINT_KEY, key);
        intent.putExtra(ProximityReceiver.GUIDANCE_POINT_NARRATIVE,
                //TODO 20170909 DPM - Localize string
                String.format("A %1$.0f metros: %2$s", radius, narrative));
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(_context.getApplicationContext(),
                        _pIntentsCollection.size(), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);

        Log.d(LOG_TAG, String.format("(%1$.6f,%2$.6f,%3$.6f)", latitude, longitude, radius));
        if (_context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "The application hasn't ACCESS_FINE_LOCATION permission!");
        }
        _locationManager.addProximityAlert(latitude, longitude, radius, -1, pendingIntent);
        _pIntentsCollection.add(pendingIntent);
    }

    public void close() {
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
